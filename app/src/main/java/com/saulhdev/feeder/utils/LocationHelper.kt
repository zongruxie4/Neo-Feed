package com.saulhdev.feeder.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class LocationHelper(private val context: Context) {

    data class ResolvedLocation(
        val latitude: Double,
        val longitude: Double,
        val cityName: String
    )

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentOrLastLocation(): ResolvedLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = try {
                    locationManager.getLastKnownLocation(provider)
                } catch (e: SecurityException) {
                    null
                }
                if (loc != null) {
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            }
        }

        val twentyMinutesAgo = System.currentTimeMillis() - 20 * 60 * 1000L
        if (bestLocation != null && bestLocation.time > twentyMinutesAgo) {
            return@withContext resolveLocation(bestLocation)
        }

        val freshLocation = requestFreshLocation(locationManager) ?: bestLocation
        if (freshLocation != null) {
            return@withContext resolveLocation(freshLocation)
        }

        null
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(locationManager: LocationManager): Location? =
        withContext(Dispatchers.Main) {
            val candidateProviders = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)
            ) {
                candidateProviders.add(LocationManager.FUSED_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                candidateProviders.add(LocationManager.NETWORK_PROVIDER)
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                candidateProviders.add(LocationManager.GPS_PROVIDER)
            }

            for (provider in candidateProviders) {
                val loc = fetchLocationWithProvider(locationManager, provider)
                if (loc != null) return@withContext loc
            }
            null
        }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocationWithProvider(
        locationManager: LocationManager,
        provider: String
    ): Location? {
        return try {
            withTimeoutOrNull(6000L.milliseconds) {
                suspendCancellableCoroutine { continuation ->
                    val cancellationSignal = CancellationSignal()
                    continuation.invokeOnCancellation {
                        cancellationSignal.cancel()
                    }

                    LocationManagerCompat.getCurrentLocation(
                        locationManager,
                        provider,
                        cancellationSignal,
                        ContextCompat.getMainExecutor(context)
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Failed to fetch location from $provider", e)
            null
        }
    }

    private suspend fun resolveLocation(location: Location): ResolvedLocation {
        val cityName = reverseGeocode(location.latitude, location.longitude)
            ?: "${String.format(Locale.US, "%.2f", location.latitude)}, ${
                String.format(
                    Locale.US,
                    "%.2f",
                    location.longitude
                )
            }"

        return ResolvedLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = cityName
        )
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val result = withTimeoutOrNull(4000L.milliseconds) {
                            suspendCancellableCoroutine<String?> { continuation ->
                                try {
                                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                                        if (continuation.isActive) {
                                            val address = addresses.firstOrNull()
                                            val name = address?.locality
                                                ?: address?.subAdminArea
                                                ?: address?.adminArea
                                            continuation.resume(name)
                                        }
                                    }
                                } catch (e: Exception) {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            }
                        }
                        if (!result.isNullOrBlank()) return@withContext result
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses: List<Address>? =
                            geocoder.getFromLocation(latitude, longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val name = address.locality ?: address.subAdminArea ?: address.adminArea
                            if (!name.isNullOrBlank()) return@withContext name
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LocationHelper", "Geocoder failed, trying fallback", e)
            }

            try {
                val lang = Locale.getDefault().language
                val url =
                    "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=$lang"
                val request = Request.Builder()
                    .url(url)
                    .header(
                        "User-Agent",
                        "NeoFeed/1.9.0 (Android; https://github.com/NeoApplications/Neo-Feed)"
                    )
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body.string()
                        val json = JSONObject(body)
                        val city = json.optString("city").ifBlank {
                            json.optString("locality").ifBlank {
                                json.optString("principalSubdivision")
                            }
                        }
                        if (city.isNotBlank()) {
                            return@withContext city
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationHelper", "Fallback reverse geocode error", e)
            }

            null
        }
}