package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
                            // Try last known location fallback
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
                // Default nature reserve / explorer location
                getDefaultLocation()
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Could not obtain GPS location: ${e.message}")
            getDefaultLocation()
        }
    }

    suspend fun getCoordinatesForQuery(cityName: String): UserLocation = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(cityName, 1)
            val address = addresses?.firstOrNull()
            if (address != null) {
                UserLocation(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    cityName = address.locality ?: address.subAdminArea ?: cityName,
                    isGpsActive = false
                )
            } else {
                getDefaultLocation().copy(cityName = cityName, isGpsActive = false)
            }
        } catch (e: Exception) {
            getDefaultLocation().copy(cityName = cityName, isGpsActive = false)
        }
    }

    private fun getCityName(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.forLanguageTag("es"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = addresses?.firstOrNull()
            address?.locality
                ?: address?.subAdminArea
                ?: address?.adminArea
                ?: "Zona Explorador (${String.format(Locale.US, "%.2f", lat)}, ${String.format(Locale.US, "%.2f", lng)})"
        } catch (e: Exception) {
            "Zona Silvestre Local"
        }
    }

    fun getDefaultLocation(): UserLocation {
        return UserLocation(
            latitude = -34.6037,
            longitude = -58.3816,
            cityName = "Reserva Costanera Sur",
            isGpsActive = false
        )
    }
}
