package com.kzeta.presionmtb.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class TrailPlace(
    val name: String,
    val temperatureC: Double,
    val latitude: Double,
    val longitude: Double,
)

class TrailException(message: String) : Exception(message)

class TrailConditions(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .callTimeout(12, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun current(): TrailPlace {
        if (!hasPermission()) {
            throw TrailException("Puedes escribir la temperatura y el lugar a mano.")
        }
        val location = locate()
            ?: throw TrailException("No encontré una posición. Escribe la temperatura a mano.")
        val name = withContext(Dispatchers.IO) { placeName(location) }
        val temperature = withContext(Dispatchers.IO) { temperature(location.latitude, location.longitude) }
        return TrailPlace(name, temperature, location.latitude, location.longitude)
    }

    private fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private suspend fun locate(): Location? {
        val manager = context.getSystemService(LocationManager::class.java)
        val provider = when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> throw TrailException("Activa la ubicación del teléfono para leer la temperatura.")
        }
        val fresh = withTimeoutOrNull(12_000) { request(manager, provider) }
        return fresh ?: manager.getLastKnownLocation(provider)
    }

    @SuppressLint("MissingPermission")
    private suspend fun request(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= 30) {
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(provider, signal, context.mainExecutor) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            } else {
                val listener = LocationListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                continuation.invokeOnCancellation {
                    manager.removeUpdates(listener)
                }
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }
        }

    @Suppress("DEPRECATION")
    private fun placeName(location: Location): String {
        return try {
            if (!Geocoder.isPresent()) return "Ubicación actual"
            val geocoder = Geocoder(context, Locale("es"))
            val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?: return "Ubicación actual"
            val locality = address.locality ?: address.subAdminArea ?: address.featureName
            listOfNotNull(locality, address.adminArea)
                .distinct()
                .joinToString(", ")
                .ifBlank { "Ubicación actual" }
        } catch (_: Exception) {
            "Ubicación actual"
        }
    }

    private fun temperature(latitude: Double, longitude: Double): Double {
        val lat = String.format(Locale.US, "%.5f", latitude)
        val lon = String.format(Locale.US, "%.5f", longitude)
        val request = Request.Builder()
            .url("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m")
            .header("User-Agent", "PresionMTB/1.0 (com.kzeta.presionmtb)")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    throw TrailException("El servicio de clima no respondió.")
                }
                return json.decodeFromString(MeteoResponse.serializer(), body).current.temperature_2m
            }
        } catch (error: TrailException) {
            throw error
        } catch (_: Exception) {
            throw TrailException("Sin conexión para consultar la temperatura.")
        }
    }
}

@Serializable
private data class MeteoResponse(val current: MeteoCurrent)

@Serializable
private data class MeteoCurrent(val temperature_2m: Double)
