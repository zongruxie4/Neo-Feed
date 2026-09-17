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

import com.saulhdev.feeder.utils.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OWMWeatherProvider : WeatherProvider() {
    override suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String
    ): WeatherData = withContext(Dispatchers.IO) {
        val apiKey = Utilities.getOWMApiKey(prefs)
        val isFahrenheit = prefs.weatherUnit.getValue() == "fahrenheit"
        val units = if (isFahrenheit) "imperial" else "metric"
        val unitSymbol = if (isFahrenheit) "°F" else "°C"
        val lang = Locale.getDefault().language

        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$latitude&lon=$longitude&units=$units&lang=$lang&appid=$apiKey"

        val request = Request.Builder()
            .url(weatherUrl)
            .header(
                "User-Agent",
                "NeoFeed/1.9.0 (Android; https://github.com/NeoApplications/Neo-Feed)"
            )
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 401) {
                    throw IllegalStateException("OpenWeatherMap API Key is invalid or not yet active")
                }
                throw IllegalStateException("OpenWeatherMap HTTP error: ${response.code}")
            }
            val body = response.body.string()
            val json = JSONObject(body)

            val main = json.getJSONObject("main")
            val temp = main.optDouble("temp", 0.0)
            val apparentTemp = main.optDouble("feels_like", temp)
            val minTemp = main.optDouble("temp_min", temp)
            val maxTemp = main.optDouble("temp_max", temp)
            val humidity = main.optInt("humidity", 0)

            val wind = json.optJSONObject("wind")
            val windSpeedRaw = wind?.optDouble("speed", 0.0) ?: 0.0
            val windSpeed = if (isFahrenheit) windSpeedRaw else windSpeedRaw * 3.6

            val rain = json.optJSONObject("rain")
            val precipitation = rain?.optDouble("1h", rain.optDouble("3h", 0.0)) ?: 0.0

            val weatherArray = json.optJSONArray("weather")
            val firstWeather = weatherArray?.optJSONObject(0)
            val owmId = firstWeather?.optInt("id", 800) ?: 800
            val icon = firstWeather?.optString("icon", "01d") ?: "01d"
            val isDay = icon.endsWith("d")

            val weatherCode = mapOwmToWmoCode(owmId)

            val hourlyList = mutableListOf<HourlyWeather>()
            try {
                val forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?" +
                        "lat=$latitude&lon=$longitude&units=$units&lang=$lang&appid=$apiKey"
                val forecastReq = Request.Builder().url(forecastUrl).build()
                client.newCall(forecastReq).execute().use { fResponse ->
                    if (fResponse.isSuccessful) {
                        val fJson = JSONObject(fResponse.body.string())
                        val list = fJson.optJSONArray("list")
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val nowSec = System.currentTimeMillis() / 1000 - 3600

                        if (list != null) {
                            var count = 0
                            for (i in 0 until list.length()) {
                                if (count >= 12) break
                                val item = list.getJSONObject(i)
                                val dt = item.optLong("dt", 0L)
                                if (dt >= nowSec) {
                                    val hMain = item.getJSONObject("main")
                                    val hTemp = hMain.optDouble("temp", 0.0)
                                    val hWeatherArr = item.optJSONArray("weather")
                                    val hFirst = hWeatherArr?.optJSONObject(0)
                                    val hOwmId = hFirst?.optInt("id", 800) ?: 800
                                    val hIcon = hFirst?.optString("icon", "01d") ?: "01d"

                                    hourlyList.add(
                                        HourlyWeather(
                                            time = timeFormat.format(Date(dt * 1000)),
                                            temperature = hTemp,
                                            weatherCode = mapOwmToWmoCode(hOwmId),
                                            isDay = hIcon.endsWith("d")
                                        )
                                    )
                                    count++
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            val finalCityName = cityName.ifBlank { json.optString("name", "Unknown") }

            WeatherData(
                cityName = finalCityName,
                temperature = temp,
                apparentTemperature = apparentTemp,
                weatherCode = weatherCode,
                humidity = humidity,
                windSpeed = windSpeed,
                precipitation = precipitation,
                isDay = isDay,
                maxTemp = maxTemp,
                minTemp = minTemp,
                unit = unitSymbol,
                hourly = hourlyList,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        }
    }

    private fun mapOwmToWmoCode(owmId: Int): Int {
        return when (owmId) {
            800 -> 0 // Clear
            801 -> 1 // Mainly clear / Few clouds
            802 -> 2 // Partly cloudy / Scattered clouds
            803, 804 -> 3 // Overcast / Broken clouds
            701, 711, 721, 741 -> 45 // Fog / Mist / Haze
            731, 751, 761, 762, 771, 781 -> 48 // Dust / Sand / Squall
            in 300..321 -> 51 // Drizzle
            500, 501, 520, 521 -> 61 // Rain: slight or moderate
            502, 503, 504, 522, 531 -> 65 // Rain: heavy
            511 -> 66 // Freezing rain
            in 600..601, 620, 621 -> 71 // Snow: slight or moderate
            602, 622 -> 75 // Snow: heavy
            in 611..616 -> 77 // Sleet / snow grains
            in 200..232 -> 95 // Thunderstorm
            else -> 2
        }
    }
}