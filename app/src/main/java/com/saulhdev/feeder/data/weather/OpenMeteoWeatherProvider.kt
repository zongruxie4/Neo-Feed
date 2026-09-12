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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class OpenMeteoProvider : WeatherProvider() {

    override suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        cityName: String
    ): WeatherData = withContext(Dispatchers.IO) {
        val tempUnitParam =
            if (prefs.weatherUnit.getValue() == "fahrenheit") "&temperature_unit=fahrenheit" else ""
        val unitSymbol = if (prefs.weatherUnit.getValue() == "fahrenheit") "°F" else "°C"
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                "&hourly=temperature_2m,weather_code,is_day" +
                "&timezone=auto$tempUnitParam"

        val request = Request.Builder()
            .url(url)
            .header(
                "User-Agent",
                "NeoFeed/1.9.0 (Android; https://github.com/NeoApplications/Neo-Feed)"
            )
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Open-Meteo HTTP error: ${response.code}")
            }
            val body = response.body.string()
            val json = JSONObject(body)

            val current = json.getJSONObject("current")
            val temp = current.optDouble("temperature_2m", 0.0)
            val apparentTemp = current.optDouble("apparent_temperature", temp)
            val weatherCode = current.optInt("weather_code", 0)
            val humidity = current.optInt("relative_humidity_2m", 0)
            val wind = current.optDouble("wind_speed_10m", 0.0)
            val precipitation = current.optDouble("precipitation", 0.0)
            val isDay = current.optInt("is_day", 1) == 1

            var maxTemp = temp
            var minTemp = temp
            if (json.has("daily")) {
                val daily = json.getJSONObject("daily")
                val maxArr = daily.optJSONArray("temperature_2m_max")
                val minArr = daily.optJSONArray("temperature_2m_min")
                if (maxArr != null && maxArr.length() > 0) maxTemp = maxArr.optDouble(0, temp)
                if (minArr != null && minArr.length() > 0) minTemp = minArr.optDouble(0, temp)
            }

            val hourlyList = mutableListOf<HourlyWeather>()
            if (json.has("hourly")) {
                val hourly = json.getJSONObject("hourly")
                val timeArr = hourly.optJSONArray("time")
                val hTempArr = hourly.optJSONArray("temperature_2m")
                val hCodeArr = hourly.optJSONArray("weather_code")
                val hDayArr = hourly.optJSONArray("is_day")

                if (timeArr != null && hTempArr != null) {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
                    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val nowMillis = System.currentTimeMillis() - 3600_000

                    var added = 0
                    for (i in 0 until timeArr.length()) {
                        if (added >= 16) break
                        val timeStr = timeArr.getString(i)
                        val date = try {
                            inputFormat.parse(timeStr)
                        } catch (_: Exception) {
                            null
                        }
                        if (date != null && date.time >= nowMillis) {
                            val formattedTime = outputFormat.format(date)
                            val hTemp = hTempArr.optDouble(i, 0.0)
                            val hCode = hCodeArr?.optInt(i, 0) ?: 0
                            val hDay = (hDayArr?.optInt(i, 1) ?: 1) == 1
                            hourlyList.add(
                                HourlyWeather(
                                    time = formattedTime,
                                    temperature = hTemp,
                                    weatherCode = hCode,
                                    isDay = hDay
                                )
                            )
                            added++
                        }
                    }
                }
            }

            WeatherData(
                cityName = cityName,
                temperature = temp,
                apparentTemperature = apparentTemp,
                weatherCode = weatherCode,
                humidity = humidity,
                windSpeed = wind,
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
}
