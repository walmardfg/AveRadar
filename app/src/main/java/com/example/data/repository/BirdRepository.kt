package com.example.data.repository

import android.util.Log
import com.example.audio.BirdAudioCatalog
import com.example.data.local.BirdDao
import com.example.data.local.BirdEntity
import com.example.data.remote.NetworkClient
import com.example.model.BirdSpecies
import com.example.model.ConservationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class BirdRepository(
    private val birdDao: BirdDao,
    private val ebirdApiToken: String? = null,
    private val iucnToken: String? = null
) {
    // Thread-safe in-memory cache for IUCN conservation status (Rate-limiting & anti-scraping compliance)
    private val iucnMemoryCache = ConcurrentHashMap<String, ConservationStatus>()
    private var lastIucnApiCallTime = 0L

    val allBirdsFlow: Flow<List<BirdSpecies>> = birdDao.getAllBirdsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteBirdsFlow: Flow<List<BirdSpecies>> = birdDao.getFavoriteBirdsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val discoveredBirdsFlow: Flow<List<BirdSpecies>> = birdDao.getDiscoveredBirdsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    fun searchBirds(query: String): Flow<List<BirdSpecies>> {
        return birdDao.searchBirdsFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun initializeCacheIfNeeded() = withContext(Dispatchers.IO) {
        val count = birdDao.getBirdCount()
        if (count == 0) {
            val entities = InitialBirdData.defaultBirds.map { BirdEntity.fromDomain(it) }
            birdDao.insertBirds(entities)
        }
    }

    suspend fun getBird(scientificName: String): BirdSpecies? = withContext(Dispatchers.IO) {
        birdDao.getBirdByScientificName(scientificName)?.toDomain()
    }

    suspend fun toggleFavorite(scientificName: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        birdDao.updateFavorite(scientificName, isFavorite)
    }

    suspend fun markDiscovered(scientificName: String) = withContext(Dispatchers.IO) {
        birdDao.updateDiscovered(scientificName, true)
    }

    suspend fun saveBird(bird: BirdSpecies) = withContext(Dispatchers.IO) {
        birdDao.insertBird(BirdEntity.fromDomain(bird))
    }

    /**
     * Consultar estado de conservación de la UICN con caché multinivel y control estricto de tasa de peticiones (Rate Limit).
     * Cumple con los lineamientos de la UICN v4:
     * - Consulta puntual bajo demanda por especie (sin scraping masivo)
     * - Caché en memoria + persistencia en Room para evitar peticiones redundantes
     * - Control de retardo (delay) de 600ms entre llamadas de red
     */
    suspend fun getOrFetchIucnStatus(scientificName: String): ConservationStatus = withContext(Dispatchers.IO) {
        if (scientificName.isBlank()) return@withContext ConservationStatus.LEAST_CONCERN

        // 1. Verificar primero en caché en memoria
        iucnMemoryCache[scientificName]?.let { return@withContext it }

        // 2. Verificar en la base de datos local Room
        val localBird = birdDao.getBirdByScientificName(scientificName)
        if (localBird != null && localBird.conservationCode.isNotBlank()) {
            val status = ConservationStatus.fromCode(localBird.conservationCode)
            iucnMemoryCache[scientificName] = status
            return@withContext status
        }

        // 3. Verificar en semillas iniciales
        val seedBird = InitialBirdData.defaultBirds.find { it.scientificName.equals(scientificName, ignoreCase = true) }
        if (seedBird != null) {
            iucnMemoryCache[scientificName] = seedBird.conservationStatus
            return@withContext seedBird.conservationStatus
        }

        // 4. Si se cuenta con token y no está en caché, realizar llamada puntual con rate limiting
        val token = iucnToken?.takeIf { it.isNotBlank() }
        if (token != null) {
            try {
                val now = System.currentTimeMillis()
                val elapsedSinceLastCall = now - lastIucnApiCallTime
                if (elapsedSinceLastCall < 600) {
                    delay(600 - elapsedSinceLastCall)
                }
                lastIucnApiCallTime = System.currentTimeMillis()

                val response = try {
                    // Intento con endpoint v4 (Authorization Header)
                    val authHeader = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    NetworkClient.iucnApi.getSpeciesStatusV4(scientificName, authHeader)
                } catch (e: Exception) {
                    // Fallback a v3
                    NetworkClient.iucnApi.getSpeciesStatusV3(scientificName, token)
                }

                val category = response.result?.firstOrNull()?.category
                if (!category.isNullOrBlank()) {
                    val status = ConservationStatus.fromCode(category)
                    iucnMemoryCache[scientificName] = status
                    return@withContext status
                }
            } catch (e: Exception) {
                Log.w("BirdRepository", "Consulta UICN para $scientificName no disponible: ${e.message}")
            }
        }

        val fallback = ConservationStatus.LEAST_CONCERN
        iucnMemoryCache[scientificName] = fallback
        return@withContext fallback
    }

    suspend fun fetchNearbyBirds(lat: Double, lng: Double): Result<List<BirdSpecies>> = withContext(Dispatchers.IO) {
        try {
            // First ensure we have basic seed data
            initializeCacheIfNeeded()

            // 1. If eBird token is configured, try official eBird observations
            val token = ebirdApiToken?.takeIf { it.isNotBlank() }
            val observations = if (token != null) {
                try {
                    NetworkClient.ebirdApi.getRecentNearbyObservations(
                        lat = lat,
                        lng = lng,
                        distKm = 35,
                        backDays = 14,
                        maxResults = 25,
                        locale = "es",
                        apiToken = token
                    )
                } catch (e: Exception) {
                    Log.w("BirdRepository", "eBird API call failed: ${e.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }

            if (observations.isNotEmpty()) {
                val uniqueObs = observations
                    .filter { !it.sciName.isNullOrBlank() }
                    .distinctBy { it.sciName }
                    .take(20)

                val enrichedBirds = uniqueObs.map { obs ->
                    async {
                        enrichBirdData(
                            sciName = obs.sciName ?: "",
                            comName = obs.comName ?: obs.sciName ?: "Ave Silvestre",
                            obsLat = obs.lat ?: lat,
                            obsLng = obs.lng ?: lng,
                            userLat = lat,
                            userLng = lng
                        )
                    }
                }.awaitAll()

                if (enrichedBirds.isNotEmpty()) {
                    birdDao.clearNonFavorites()
                    birdDao.insertBirds(enrichedBirds.map { BirdEntity.fromDomain(it) })
                    return@withContext Result.success(enrichedBirds)
                }
            }

            // 2. Query open worldwide iNaturalist species counts for this exact location (No API key needed!)
            val inatBirds = try {
                val inatResponse = NetworkClient.iNaturalistApi.getNearbySpeciesCounts(
                    lat = lat,
                    lng = lng,
                    radiusKm = 60,
                    locale = "es",
                    perPage = 25
                )
                val results = inatResponse.results.orEmpty().filter { it.taxon != null && !it.taxon.name.isNullOrBlank() }
                results.mapIndexed { index, item ->
                    val taxon = item.taxon!!
                    val sciName = taxon.name ?: "Avis sp."
                    val commonNameRaw = taxon.preferredCommonName ?: sciName
                    val commonName = commonNameRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                    val photoList = mutableListOf<String>()
                    taxon.defaultPhoto?.mediumUrl?.let { photoList.add(it) }
                    taxon.taxonPhotos?.forEach { tp ->
                        tp.photo?.mediumUrl?.let { photoList.add(it) }
                    }
                    val distinctPhotos = if (photoList.isNotEmpty()) {
                        photoList.distinct()
                    } else {
                        listOf("https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=800&auto=format&fit=crop&q=80")
                    }

                    val desc = if (!taxon.wikipediaSummary.isNullOrBlank()) {
                        cleanWikipediaSummary(taxon.wikipediaSummary)
                    } else {
                        "Especie de ave autóctona y silvestre registrada en esta región. Cumple un rol vital en el equilibrio ecológico dispersando semillas y controlando poblaciones de insectos."
                    }

                    val conservation = if (taxon.conservationStatus?.status != null) {
                        ConservationStatus.fromCode(taxon.conservationStatus.status)
                    } else {
                        ConservationStatus.LEAST_CONCERN
                    }

                    val dist = 0.3 + (index * 0.7)
                    val resolvedAudio = BirdAudioCatalog.getAudioUrl(sciName)
                        ?: InitialBirdData.defaultBirds.firstOrNull { it.scientificName.equals(sciName, ignoreCase = true) }?.audioUrl

                    BirdSpecies(
                        scientificName = sciName,
                        commonName = commonName,
                        familyName = "Aves",
                        description = desc,
                        conservationStatus = conservation,
                        audioUrl = resolvedAudio,
                        photoUrls = distinctPhotos,
                        soundDuration = "0:15",
                        funFact = "Confirmada ${item.count ?: 1} veces en esta zona por la comunidad científica y observadores de aves.",
                        wingspan = "22 - 45 cm",
                        diet = "Semillas, insectos y brotes silvestres",
                        distanceKm = Math.round(dist * 10.0) / 10.0,
                        isDiscovered = index < 2,
                        isFavorite = false
                    )
                }
            } catch (e: Exception) {
                Log.w("BirdRepository", "iNaturalist nearby species query error: ${e.message}")
                emptyList()
            }

            if (inatBirds.isNotEmpty()) {
                Log.d("BirdRepository", "Loaded ${inatBirds.size} birds from iNaturalist for location ($lat, $lng)")
                birdDao.clearNonFavorites()
                birdDao.insertBirds(inatBirds.map { BirdEntity.fromDomain(it) })
                return@withContext Result.success(inatBirds)
            }

            // 3. Fallback: Return cached / seed list with simulated distance calculation
            val cached = InitialBirdData.defaultBirds.map { bird ->
                bird.copy(distanceKm = calculateSimulatedDistance(lat, lng, bird.scientificName))
            }
            birdDao.clearNonFavorites()
            birdDao.insertBirds(cached.map { BirdEntity.fromDomain(it) })
            Result.success(cached)
        } catch (e: Exception) {
            Log.e("BirdRepository", "Error fetching nearby birds", e)
            Result.failure(e)
        }
    }

    private suspend fun enrichBirdData(
        sciName: String,
        comName: String,
        obsLat: Double,
        obsLng: Double,
        userLat: Double,
        userLng: Double
    ): BirdSpecies {
        // Check if we already have it in default database
        val existingSeed = InitialBirdData.defaultBirds.find { it.scientificName.equals(sciName, ignoreCase = true) }
        val distance = calculateDistanceKm(userLat, userLng, obsLat, obsLng)

        var photoUrls = existingSeed?.photoUrls ?: emptyList()
        var description = existingSeed?.description
        var familyName = existingSeed?.familyName ?: ""
        var conservation = existingSeed?.conservationStatus ?: ConservationStatus.LEAST_CONCERN
        var audioUrl = existingSeed?.audioUrl

        // 1. Enrich with iNaturalist if photos or description are missing
        if (photoUrls.isEmpty() || description == null) {
            try {
                val inatResponse = NetworkClient.iNaturalistApi.searchTaxa(query = sciName, locale = "es")
                val taxon = inatResponse.results?.firstOrNull()
                if (taxon != null) {
                    val photos = mutableListOf<String>()
                    taxon.defaultPhoto?.mediumUrl?.let { photos.add(it) }
                    taxon.taxonPhotos?.forEach { tp ->
                        tp.photo?.mediumUrl?.let { photos.add(it) }
                    }
                    if (photos.isNotEmpty()) {
                        photoUrls = photos.distinct()
                    }
                    if (taxon.wikipediaSummary != null && description == null) {
                        description = cleanWikipediaSummary(taxon.wikipediaSummary)
                    }
                    if (taxon.conservationStatus?.status != null) {
                        conservation = ConservationStatus.fromCode(taxon.conservationStatus.status)
                    }
                }
            } catch (e: Exception) {
                Log.w("BirdRepository", "iNaturalist fetch error for $sciName: ${e.message}")
            }
        }

        // 2. Enrich with bird song audio if missing
        if (audioUrl == null) {
            audioUrl = BirdAudioCatalog.getAudioUrl(sciName)
            if (audioUrl == null) {
                try {
                    val inatObs = NetworkClient.iNaturalistApi.getObservationsWithSound(taxonName = sciName)
                    val soundUrl = inatObs.results?.firstOrNull()?.sounds?.firstOrNull()?.fileUrl
                    if (!soundUrl.isNullOrBlank()) {
                        audioUrl = soundUrl
                    }
                } catch (e: Exception) {
                    Log.w("BirdRepository", "iNat sound search error for $sciName: ${e.message}")
                }
            }
        }

        // 3. Fallback friendly description if still empty
        if (description.isNullOrBlank()) {
            description = "Un ave fascinante que habita en esta región natural. Suele cantar al amanecer y al atardecer entre las copas de los árboles y arbustos nativos.\n\nSe alimenta activamente en su hábitat silvestre, cumpliendo un rol ecológico fundamental como dispersora de semillas y controladora de insectos."
        }

        // 4. Default photo if still empty
        if (photoUrls.isEmpty()) {
            photoUrls = listOf("https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=800&auto=format&fit=crop&q=80")
        }

        return BirdSpecies(
            scientificName = sciName,
            commonName = comName,
            familyName = familyName,
            description = description,
            conservationStatus = conservation,
            audioUrl = audioUrl ?: existingSeed?.audioUrl,
            photoUrls = photoUrls,
            soundDuration = existingSeed?.soundDuration ?: "0:15",
            funFact = existingSeed?.funFact ?: "¡Forma parte de la biodiversidad observada en tus alrededores!",
            wingspan = existingSeed?.wingspan ?: "20 - 35 cm",
            diet = existingSeed?.diet ?: "Semillas, frutos e insectos",
            distanceKm = distance,
            isFavorite = existingSeed?.isFavorite ?: false,
            isDiscovered = existingSeed?.isDiscovered ?: false
        )
    }

    private fun cleanWikipediaSummary(raw: String): String {
        val clean = raw.replace(Regex("<[^>]*>"), "")
            .replace("\n\n", " ")
            .trim()
        val sentences = clean.split(Regex("(?<=[.!?])\\s+"))
        return if (sentences.size > 2) {
            val part1 = sentences.take(2).joinToString(" ")
            val part2 = sentences.drop(2).take(2).joinToString(" ")
            if (part2.isNotBlank()) "$part1\n\n$part2" else part1
        } else {
            clean
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = r * c
        return Math.round(distance * 10.0) / 10.0
    }

    private fun calculateSimulatedDistance(userLat: Double, userLng: Double, name: String): Double {
        val hash = Math.abs((name + userLat.toInt() + userLng.toInt()).hashCode()) % 50
        return (hash / 10.0) + 0.4
    }
}
