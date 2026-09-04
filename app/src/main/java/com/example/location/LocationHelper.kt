package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.example.data.remote.NetworkClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.Normalizer
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val isGpsActive: Boolean = true
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation = withContext(Dispatchers.IO) {
        try {
            val location: Location? = suspendCancellableCoroutine { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            continuation.resume(loc)
                        } else {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    continuation.resume(lastLoc)
                                }
                                .addOnFailureListener {
                                    continuation.resume(null)
                                }
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                val cityName = getCityName(location.latitude, location.longitude)
                UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = cityName,
                    isGpsActive = true
                )
            } else {
                getDefaultLocation()
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Could not obtain GPS location: ${e.message}")
            getDefaultLocation()
        }
    }

    suspend fun getCoordinatesForQuery(rawQuery: String): UserLocation = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.isBlank()) return@withContext getDefaultLocation()

        // 1. First check offline known major cities for instant zero-latency match
        val offlineDirect = findOfflineCity(query)
        if (offlineDirect != null) {
            Log.d("LocationHelper", "Offline dictionary matched '$query' -> ${offlineDirect.cityName}")
            return@withContext offlineDirect
        }

        // 2. Open-Meteo Geocoding API (Ultra fast, global, zero-configuration)
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=es&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BirdRadarApp/1.0")
                .build()

            val response = NetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                response.close()
                if (!bodyStr.isNullOrBlank()) {
                    val json = JSONObject(bodyStr)
                    val results = json.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val item = results.getJSONObject(0)
                        val lat = item.getDouble("latitude")
                        val lng = item.getDouble("longitude")
                        val name = item.optString("name")
                        val country = item.optString("country")
                        val admin1 = item.optString("admin1")

                        val formattedName = buildString {
                            append(name)
                            if (admin1.isNotBlank() && !admin1.equals(name, ignoreCase = true)) {
                                append(", ").append(admin1)
                            }
                            if (country.isNotBlank()) {
                                append(" (").append(country).append(")")
                            }
                        }

                        Log.d("LocationHelper", "Open-Meteo resolved '$query' -> ($lat, $lng): $formattedName")
                        return@withContext UserLocation(
                            latitude = lat,
                            longitude = lng,
                            cityName = formattedName,
                            isGpsActive = false
                        )
                    }
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Open-Meteo geocode network error: ${e.message}")
        }

        // 3. OpenStreetMap Nominatim Geocoding API
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1&accept-language=es"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BirdRadarAndroid/1.0 (biodiversity-explorer)")
                .build()

            val response = NetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                response.close()
                if (!bodyStr.isNullOrBlank()) {
                    val array = JSONArray(bodyStr)
                    if (array.length() > 0) {
                        val item = array.getJSONObject(0)
                        val lat = item.getDouble("lat")
                        val lng = item.getDouble("lon")
                        val displayName = item.optString("display_name")
                        val shortName = displayName.split(",").take(2).joinToString(", ").trim()

                        Log.d("LocationHelper", "Nominatim resolved '$query' -> ($lat, $lng): $shortName")
                        return@withContext UserLocation(
                            latitude = lat,
                            longitude = lng,
                            cityName = if (shortName.isNotBlank()) shortName else query,
                            isGpsActive = false
                        )
                    }
                }
            } else {
                response.close()
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Nominatim geocode network error: ${e.message}")
        }

        // 4. Android System Geocoder (Fallback)
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val locality = address.locality ?: address.subAdminArea ?: address.adminArea ?: query
                    val country = address.countryName
                    val displayName = if (country != null) "$locality ($country)" else locality
                    return@withContext UserLocation(
                        latitude = address.latitude,
                        longitude = address.longitude,
                        cityName = displayName,
                        isGpsActive = false
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "System Geocoder error: ${e.message}")
        }

        // Fallback default
        getDefaultLocation().copy(cityName = query, isGpsActive = false)
    }

    private suspend fun getCityName(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        // 1. Android System Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.forLanguageTag("es"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val locality = address.locality ?: address.subAdminArea ?: address.adminArea
                    val country = address.countryName
                    if (locality != null) {
                        return@withContext if (country != null) "$locality ($country)" else locality
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Nominatim Reverse Geocoding
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&accept-language=es"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BirdRadarAndroid/1.0")
                .build()

            val response = NetworkClient.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string()
                response.close()
                if (!bodyStr.isNullOrBlank()) {
                    val json = JSONObject(bodyStr)
                    val addressObj = json.optJSONObject("address")
                    val city = addressObj?.optString("city")?.takeIf { it.isNotBlank() }
                        ?: addressObj?.optString("town")?.takeIf { it.isNotBlank() }
                        ?: addressObj?.optString("village")?.takeIf { it.isNotBlank() }
                        ?: addressObj?.optString("county")?.takeIf { it.isNotBlank() }
                        ?: addressObj?.optString("state")?.takeIf { it.isNotBlank() }
                    val country = addressObj?.optString("country")
                    if (city != null) {
                        return@withContext if (!country.isNullOrBlank()) "$city ($country)" else city
                    }
                }
            } else {
                response.close()
            }
        } catch (_: Exception) {}

        "Zona Silvestre (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lng)})"
    }

    fun getDefaultLocation(): UserLocation {
        return UserLocation(
            latitude = -34.6037,
            longitude = -58.3816,
            cityName = "Reserva Costanera Sur, Buenos Aires",
            isGpsActive = false
        )
    }

    private fun findOfflineCity(query: String): UserLocation? {
        val clean = normalizeText(query)
        for ((key, loc) in OFFLINE_CITIES) {
            if (clean == key || clean.contains(key) || key.contains(clean)) {
                return loc
            }
        }
        return null
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .trim()
    }

    companion object {
        private val OFFLINE_CITIES = mapOf(
            // Argentina
            "bariloche" to UserLocation(-41.1456, -71.3082, "Bariloche, Río Negro (Argentina)", false),
            "san carlos de bariloche" to UserLocation(-41.1456, -71.3082, "Bariloche, Río Negro (Argentina)", false),
            "buenos aires" to UserLocation(-34.6037, -58.3816, "Buenos Aires (Argentina)", false),
            "costanera sur" to UserLocation(-34.6111, -58.3533, "Reserva Costanera Sur, Buenos Aires", false),
            "cordoba" to UserLocation(-31.4201, -64.1888, "Córdoba (Argentina)", false),
            "rosario" to UserLocation(-32.9442, -60.6505, "Rosario, Santa Fe (Argentina)", false),
            "mendoza" to UserLocation(-32.8895, -68.8458, "Mendoza (Argentina)", false),
            "salta" to UserLocation(-24.7821, -65.4232, "Salta (Argentina)", false),
            "jujuy" to UserLocation(-24.1858, -65.2995, "San Salvador de Jujuy (Argentina)", false),
            "ushuaia" to UserLocation(-54.8019, -68.3030, "Ushuaia, Tierra del Fuego (Argentina)", false),
            "calafate" to UserLocation(-50.3379, -72.2648, "El Calafate, Santa Cruz (Argentina)", false),
            "el calafate" to UserLocation(-50.3379, -72.2648, "El Calafate, Santa Cruz (Argentina)", false),
            "iguazu" to UserLocation(-25.5976, -54.5782, "Puerto Iguazú, Misiones (Argentina)", false),
            "puerto madryn" to UserLocation(-42.7692, -65.0385, "Puerto Madryn, Chubut (Argentina)", false),
            "mar del plata" to UserLocation(-38.0055, -57.5560, "Mar del Plata, Buenos Aires (Argentina)", false),
            "la plata" to UserLocation(-34.9214, -57.9545, "La Plata, Buenos Aires (Argentina)", false),
            "neuquen" to UserLocation(-38.9516, -68.0591, "Neuquén (Argentina)", false),
            "tucuman" to UserLocation(-26.8083, -65.2176, "San Miguel de Tucumán (Argentina)", false),
            "chalten" to UserLocation(-49.3315, -72.8864, "El Chaltén, Santa Cruz (Argentina)", false),
            "villa la angostura" to UserLocation(-40.7628, -71.6447, "Villa La Angostura, Neuquén (Argentina)", false),
            "san martin de los andes" to UserLocation(-40.1582, -71.3533, "San Martín de los Andes, Neuquén (Argentina)", false),

            // Chile
            "santiago" to UserLocation(-33.4489, -70.6693, "Santiago (Chile)", false),
            "santiago de chile" to UserLocation(-33.4489, -70.6693, "Santiago (Chile)", false),
            "valparaiso" to UserLocation(-33.0472, -71.6127, "Valparaíso (Chile)", false),
            "vina del mar" to UserLocation(-33.0245, -71.5518, "Viña del Mar (Chile)", false),
            "concepcion" to UserLocation(-36.8201, -73.0444, "Concepción (Chile)", false),
            "punta arenas" to UserLocation(-53.1638, -70.9171, "Punta Arenas (Chile)", false),
            "puerto montt" to UserLocation(-41.4693, -72.9424, "Puerto Montt (Chile)", false),
            "la serena" to UserLocation(-29.9027, -71.2519, "La Serena (Chile)", false),
            "antofagasta" to UserLocation(-23.6509, -70.3975, "Antofagasta (Chile)", false),

            // Colombia
            "bogota" to UserLocation(4.7110, -74.0721, "Bogotá (Colombia)", false),
            "medellin" to UserLocation(6.2442, -75.5812, "Medellín, Antioquia (Colombia)", false),
            "cali" to UserLocation(3.4516, -76.5320, "Cali, Valle del Cauca (Colombia)", false),
            "cartagena" to UserLocation(10.3910, -75.4794, "Cartagena de Indias (Colombia)", false),
            "barranquilla" to UserLocation(10.9685, -74.7813, "Barranquilla (Colombia)", false),
            "bucaramanga" to UserLocation(7.1254, -73.1198, "Bucaramanga, Santander (Colombia)", false),
            "santa marta" to UserLocation(11.2408, -74.1990, "Santa Marta (Colombia)", false),
            "pereira" to UserLocation(4.8133, -75.6961, "Pereira, Risaralda (Colombia)", false),
            "manizales" to UserLocation(5.0689, -75.5174, "Manizales, Caldas (Colombia)", false),

            // México
            "ciudad de mexico" to UserLocation(19.4326, -99.1332, "Ciudad de México (México)", false),
            "cdmx" to UserLocation(19.4326, -99.1332, "Ciudad de México (México)", false),
            "mexico" to UserLocation(19.4326, -99.1332, "Ciudad de México (México)", false),
            "guadalajara" to UserLocation(20.6597, -103.3496, "Guadalajara, Jalisco (México)", false),
            "monterrey" to UserLocation(25.6866, -100.3161, "Monterrey, Nuevo León (México)", false),
            "cancun" to UserLocation(21.1619, -86.8515, "Cancún, Quintana Roo (México)", false),
            "puebla" to UserLocation(19.0414, -98.2063, "Puebla (México)", false),
            "merida" to UserLocation(20.9674, -89.5926, "Mérida, Yucatán (México)", false),
            "oaxaca" to UserLocation(17.0732, -96.7266, "Oaxaca de Juárez (México)", false),
            "tijuana" to UserLocation(32.5149, -117.0382, "Tijuana, Baja California (México)", false),

            // Perú
            "lima" to UserLocation(-12.0464, -77.0428, "Lima (Perú)", false),
            "cusco" to UserLocation(-13.5319, -71.9675, "Cusco (Perú)", false),
            "cuzco" to UserLocation(-13.5319, -71.9675, "Cusco (Perú)", false),
            "arequipa" to UserLocation(-16.4090, -71.5375, "Arequipa (Perú)", false),
            "trujillo" to UserLocation(-8.1160, -79.0300, "Trujillo (Perú)", false),
            "iquitos" to UserLocation(-3.7491, -73.2538, "Iquitos, Loreto (Perú)", false),

            // España
            "madrid" to UserLocation(40.4168, -3.7038, "Madrid (España)", false),
            "barcelona" to UserLocation(41.3879, 2.1699, "Barcelona, Cataluña (España)", false),
            "valencia" to UserLocation(39.4699, -0.3763, "Valencia (España)", false),
            "sevilla" to UserLocation(37.3891, -5.9845, "Sevilla, Andalucía (España)", false),
            "granada" to UserLocation(37.1773, -3.5986, "Granada, Andalucía (España)", false),
            "bilbao" to UserLocation(43.2630, -2.9350, "Bilbao, País Vasco (España)", false),
            "malaga" to UserLocation(36.7213, -4.4214, "Málaga, Andalucía (España)", false),
            "zaragoza" to UserLocation(41.6488, -0.8891, "Zaragoza, Aragón (España)", false),
            "mallorca" to UserLocation(39.6953, 3.0176, "Palma de Mallorca (España)", false),
            "canarias" to UserLocation(28.2916, -16.6291, "Tenerife, Islas Canarias (España)", false),

            // Uruguay
            "montevideo" to UserLocation(-34.9011, -56.1645, "Montevideo (Uruguay)", false),
            "punta del este" to UserLocation(-34.9657, -54.9450, "Punta del Este, Maldonado (Uruguay)", false),
            "colonia" to UserLocation(-34.4626, -57.8400, "Colonia del Sacramento (Uruguay)", false),

            // Otros países
            "caracas" to UserLocation(10.4806, -66.9036, "Caracas (Venezuela)", false),
            "quito" to UserLocation(-0.1807, -78.4678, "Quito (Ecuador)", false),
            "guayaquil" to UserLocation(-2.1894, -79.8891, "Guayaquil (Ecuador)", false),
            "la paz" to UserLocation(-16.5000, -68.1500, "La Paz (Bolivia)", false),
            "santa cruz" to UserLocation(-17.7863, -63.1812, "Santa Cruz de la Sierra (Bolivia)", false),
            "asuncion" to UserLocation(-25.2637, -57.5759, "Asunción (Paraguay)", false),
            "san jose" to UserLocation(9.9281, -84.0907, "San José (Costa Rica)", false),
            "panama" to UserLocation(8.9824, -79.5199, "Ciudad de Panamá (Panamá)", false),
            "miami" to UserLocation(25.7617, -80.1918, "Miami, Florida (EE.UU.)", false),
            "nueva york" to UserLocation(40.7128, -74.0060, "Nueva York (EE.UU.)", false)
        )
    }
}
