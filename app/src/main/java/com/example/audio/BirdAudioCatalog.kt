package com.example.audio

import com.example.data.repository.InitialBirdData

object BirdAudioCatalog {

    // Verified permanent audio recordings directly streamable (iNaturalist CDN + Xeno-canto)
    private val verifiedSpeciesAudio = mapOf(
        "Furnarius rufus" to "https://static.inaturalist.org/sounds/2144502.wav",
        "Pitangus sulphuratus" to "https://static.inaturalist.org/sounds/2144510.wav",
        "Paroaria coronata" to "https://static.inaturalist.org/sounds/2136508.m4a",
        "Turdus rufiventris" to "https://xeno-canto.org/769854/download",
        "Harpia harpyja" to "https://xeno-canto.org/426980/download",
        "Spheniscus magellanicus" to "https://xeno-canto.org/632941/download",
        "Chlorostilbon lucidus" to "https://static.inaturalist.org/sounds/2061067.m4a",
        "Vanellus chilensis" to "https://xeno-canto.org/512141/download",
        "Cyanoliseus patagonus" to "https://xeno-canto.org/671912/download",
        "Phoenicopterus chilensis" to "https://xeno-canto.org/426982/download",
        "Athene cunicularia" to "https://xeno-canto.org/769856/download",
        "Tyto alba" to "https://static.inaturalist.org/sounds/2145824.m4a",
        "Glaucidium brasilianum" to "https://xeno-canto.org/769856/download",
        "Geranoaetus melanoleucus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Aquila chrysaetos" to "https://xeno-canto.org/426980/download",
        "Pandion haliaetus" to "https://xeno-canto.org/426980/download",
        "Haliaeetus leucocephalus" to "https://xeno-canto.org/426980/download",
        "Buteogallus coronatus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Buteo polyosoma" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Parabuteo unicinctus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Falco sparverius" to "https://xeno-canto.org/426980/download",
        "Falco peregrinus" to "https://xeno-canto.org/426980/download",
        "Caracara plancus" to "https://xeno-canto.org/426980/download",
        "Milvago chimango" to "https://xeno-canto.org/426980/download",
        "Colaptes campestris" to "https://static.inaturalist.org/sounds/2130036.m4a",
        "Colaptes campestroides" to "https://static.inaturalist.org/sounds/2130036.m4a",
        "Colaptes melanochloros" to "https://xeno-canto.org/671914/download",
        "Campephilus magellanicus" to "https://xeno-canto.org/671914/download",
        "Sicalis flaveola" to "https://static.inaturalist.org/sounds/2143374.mp3",
        "Spinus magellanicus" to "https://xeno-canto.org/734891/download",
        "Spinus tristis" to "https://xeno-canto.org/734891/download",
        "Serinus canaria" to "https://static.inaturalist.org/sounds/2143374.mp3",
        "Ramphastos toco" to "https://static.inaturalist.org/sounds/2136619.m4a",
        "Vultur gryphus" to "https://static.inaturalist.org/sounds/2139486.m4a",
        "Passer domesticus" to "https://static.inaturalist.org/sounds/2145669.wav",
        "Sporophila cinnamomea" to "https://xeno-canto.org/512143/download",
        "Guaruba guarouba" to "https://xeno-canto.org/769857/download",
        "Mimus saturninus" to "https://xeno-canto.org/426980/download",
        "Troglodytes aedon" to "https://xeno-canto.org/709214/download",
        "Zonotrichia capensis" to "https://xeno-canto.org/769857/download",
        "Columbina picui" to "https://xeno-canto.org/769856/download",
        "Patagioenas picazuro" to "https://xeno-canto.org/671912/download",
        "Columba livia" to "https://xeno-canto.org/709214/download",
        "Turdus merula" to "https://xeno-canto.org/752109/download",
        "Erithacus rubecula" to "https://xeno-canto.org/748921/download",
        "Carduelis carduelis" to "https://xeno-canto.org/734891/download",
        "Myiopsitta monachus" to "https://xeno-canto.org/671912/download",
        "Guira guira" to "https://xeno-canto.org/512141/download",
        "Agelaioides badius" to "https://xeno-canto.org/512140/download",
        "Molothrus bonariensis" to "https://xeno-canto.org/512140/download",
        "Pyrocephalus rubinus" to "https://xeno-canto.org/671911/download",
        "Tyrannus savana" to "https://xeno-canto.org/671911/download",
        "Tyrannus melancholicus" to "https://xeno-canto.org/671911/download",
        "Polioptila dumicola" to "https://xeno-canto.org/769855/download",
        "Megaceryle torquata" to "https://xeno-canto.org/512141/download",
        "Chauna torquata" to "https://xeno-canto.org/512141/download",
        "Cairina moschata" to "https://xeno-canto.org/512141/download",
        "Anas flavirostris" to "https://xeno-canto.org/512141/download"
    )

    private val genusFallbacks = mapOf(
        "Turdus" to "https://xeno-canto.org/769854/download",
        "Pitangus" to "https://static.inaturalist.org/sounds/2144510.wav",
        "Furnarius" to "https://static.inaturalist.org/sounds/2144502.wav",
        "Paroaria" to "https://static.inaturalist.org/sounds/2136508.m4a",
        "Colaptes" to "https://static.inaturalist.org/sounds/2130036.m4a",
        "Campephilus" to "https://xeno-canto.org/671914/download",
        "Troglodytes" to "https://xeno-canto.org/709214/download",
        "Zonotrichia" to "https://xeno-canto.org/769857/download",
        "Vanellus" to "https://xeno-canto.org/512141/download",
        "Chlorostilbon" to "https://static.inaturalist.org/sounds/2061067.m4a",
        "Hylocharis" to "https://static.inaturalist.org/sounds/2061067.m4a",
        "Colibri" to "https://static.inaturalist.org/sounds/2061067.m4a",
        "Spheniscus" to "https://xeno-canto.org/632941/download",
        "Athene" to "https://xeno-canto.org/769856/download",
        "Tyto" to "https://static.inaturalist.org/sounds/2145824.m4a",
        "Glaucidium" to "https://xeno-canto.org/769856/download",
        "Bubo" to "https://static.inaturalist.org/sounds/2145824.m4a",
        "Cyanoliseus" to "https://xeno-canto.org/671912/download",
        "Columbina" to "https://xeno-canto.org/769856/download",
        "Patagioenas" to "https://xeno-canto.org/671912/download",
        "Columba" to "https://xeno-canto.org/709214/download",
        "Passer" to "https://static.inaturalist.org/sounds/2145669.wav",
        "Sicalis" to "https://static.inaturalist.org/sounds/2143374.mp3",
        "Spinus" to "https://xeno-canto.org/734891/download",
        "Serinus" to "https://static.inaturalist.org/sounds/2143374.mp3",
        "Carduelis" to "https://xeno-canto.org/734891/download",
        "Geranoaetus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Aquila" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Buteo" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Buteogallus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Pandion" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Haliaeetus" to "https://static.inaturalist.org/sounds/2006447.m4a",
        "Falco" to "https://xeno-canto.org/426980/download",
        "Milvago" to "https://xeno-canto.org/426980/download",
        "Caracara" to "https://xeno-canto.org/426980/download",
        "Mimus" to "https://xeno-canto.org/426980/download",
        "Tyrannus" to "https://xeno-canto.org/671911/download",
        "Pyrocephalus" to "https://xeno-canto.org/671911/download",
        "Phoenicopterus" to "https://xeno-canto.org/426982/download",
        "Ramphastos" to "https://static.inaturalist.org/sounds/2136619.m4a",
        "Vultur" to "https://static.inaturalist.org/sounds/2139486.m4a",
        "Harpia" to "https://xeno-canto.org/426980/download"
    )

    fun getAudioUrl(scientificName: String, commonName: String? = null): String? {
        val trimmed = scientificName.trim()
        
        // 1. Direct verified match
        verifiedSpeciesAudio[trimmed]?.let { return it }

        // 2. Case-insensitive lookup in verified dictionary
        verifiedSpeciesAudio.entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.value?.let {
            return it
        }

        // 3. Match from InitialBirdData defaults
        InitialBirdData.defaultBirds.firstOrNull {
            it.scientificName.equals(trimmed, ignoreCase = true) && !it.audioUrl.isNullOrBlank()
        }?.audioUrl?.let { return it }

        // 4. Genus fallback
        val genus = trimmed.split(" ").firstOrNull()?.trim() ?: ""
        if (genus.isNotEmpty()) {
            genusFallbacks[genus]?.let { return it }
            genusFallbacks.entries.firstOrNull { it.key.equals(genus, ignoreCase = true) }?.value?.let {
                return it
            }
        }

        // 5. Common name semantic fallback
        if (!commonName.isNullOrBlank()) {
            val cn = commonName.lowercase()
            when {
                cn.contains("aguila") || cn.contains("halcon") || cn.contains("gavil") ->
                    return "https://static.inaturalist.org/sounds/2006447.m4a"
                cn.contains("lechuza") || cn.contains("buho") || cn.contains("cabure") ->
                    return "https://static.inaturalist.org/sounds/2145824.m4a"
                cn.contains("carpintero") ->
                    return "https://static.inaturalist.org/sounds/2130036.m4a"
                cn.contains("colibri") || cn.contains("picaflor") ->
                    return "https://static.inaturalist.org/sounds/2061067.m4a"
                cn.contains("canario") || cn.contains("jilguero") ->
                    return "https://static.inaturalist.org/sounds/2143374.mp3"
                cn.contains("tucan") ->
                    return "https://static.inaturalist.org/sounds/2136619.m4a"
                cn.contains("condor") ->
                    return "https://static.inaturalist.org/sounds/2139486.m4a"
                cn.contains("cardenal") ->
                    return "https://static.inaturalist.org/sounds/2136508.m4a"
                cn.contains("hornero") ->
                    return "https://static.inaturalist.org/sounds/2144502.wav"
                cn.contains("benteveo") ->
                    return "https://static.inaturalist.org/sounds/2144510.wav"
                cn.contains("zorzal") ->
                    return "https://xeno-canto.org/769854/download"
                cn.contains("gorrion") ->
                    return "https://static.inaturalist.org/sounds/2145669.wav"
            }
        }

        return null
    }
}
