package com.example.data.network

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.example.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

class LocationSearchService(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun searchLocation(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        // 1. First Attempt: Android Built-in System Geocoder (zero network block risk, powered by Google Play)
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addressList: List<Address>? = geocoder.getFromLocationName(trimmed, 5)
                if (!addressList.isNullOrEmpty()) {
                    val nativeResults = addressList.mapIndexed { index, address ->
                        val label = buildAddressLabel(address)
                        SearchResult(
                            placeId = index.toLong() + 1000L,
                            displayName = label,
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    }
                    if (nativeResults.isNotEmpty()) {
                        return@withContext nativeResults
                    }
                }
            }
        } catch (e: Exception) {
            // Geocoder service may throw IOException on some emulators; proceed to HTTP fallback
        }

        // 2. Second Attempt: Nominatim OpenStreetMap Geocoding Fallback
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5&addressdetails=1"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZoneQuietApp/1.0 (Android; contact=zonequiet@example.com)")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()

                val results = mutableListOf<SearchResult>()
                val jsonArray = JSONArray(body)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val placeId = item.optLong("place_id", i.toLong())
                    val displayName = item.optString("display_name", "Unknown Location")
                    val lat = item.optDouble("lat", 0.0)
                    val lon = item.optDouble("lon", 0.0)

                    if (lat != 0.0 || lon != 0.0) {
                        results.add(SearchResult(placeId, displayName, lat, lon))
                    }
                }
                results
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildAddressLabel(address: Address): String {
        val parts = mutableListOf<String>()
        if (!address.featureName.isNullOrBlank() && address.featureName != address.subThoroughfare) {
            parts.add(address.featureName)
        }
        if (!address.thoroughfare.isNullOrBlank()) {
            val street = if (!address.subThoroughfare.isNullOrBlank()) {
                "${address.subThoroughfare} ${address.thoroughfare}"
            } else {
                address.thoroughfare
            }
            if (!parts.contains(street)) parts.add(street)
        }
        if (!address.locality.isNullOrBlank() && !parts.contains(address.locality)) {
            parts.add(address.locality)
        }
        if (!address.adminArea.isNullOrBlank() && !parts.contains(address.adminArea)) {
            parts.add(address.adminArea)
        }
        if (!address.countryName.isNullOrBlank() && !parts.contains(address.countryName)) {
            parts.add(address.countryName)
        }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else (address.getAddressLine(0) ?: "Selected Location")
    }
}
