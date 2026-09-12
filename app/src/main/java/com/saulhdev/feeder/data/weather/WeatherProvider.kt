/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   NeoApplications Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saulhdev.feeder.data.weather

import com.saulhdev.feeder.data.content.FeedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit

abstract class WeatherProvider {
    open val isEnabled: Boolean = true
    val prefs: FeedPreferences by inject(FeedPreferences::class.java)

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    abstract suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String
    ): WeatherData

    suspend fun getCoordinatesForCity(cityName: String): GeoResult? = withContext(Dispatchers.IO) {
        val encodedCity = java.net.URLEncoder.encode(cityName.trim(), "UTF-8")
        val lang = java.util.Locale.getDefault().language
        val url =
            "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=$lang&format=json"
        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "NeoFeed/1.9.0 (Android; https://github.com/NeoApplications/Neo-Feed)"
            )
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body.string()
                val json = JSONObject(body)
                if (!json.has("results")) return@withContext null
                val results = json.getJSONArray("results")
                if (results.length() == 0) return@withContext null
                val first = results.getJSONObject(0)
                GeoResult(
                    name = first.optString("name", cityName),
                    latitude = first.getDouble("latitude"),
                    longitude = first.getDouble("longitude"),
                    country = if (first.has("country")) first.getString("country") else null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null
)
