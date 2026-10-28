package com.example.data.network

import com.example.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class LocationSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchLocation(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=5&addressdetails=1"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ZoneQuiet-Android/1.0 (Location Search)")
                .build()

            client.newCall(request).execute().use { response ->
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
}
