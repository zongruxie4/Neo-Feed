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

import android.util.Log
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.utils.LocationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WeatherRepository(
    private val prefs: FeedPreferences,
    private val locationHelper: LocationHelper,
    private val openMeteoProvider: OpenMeteoProvider,
    private val owmWeatherProvider: OWMWeatherProvider
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val mutex = Mutex()
    private var lastFetchTime = 0L
    private val cacheDurationMillis = 30 * 60 * 1000L // 30 minutes

    fun getActiveProvider(): WeatherProvider {
        val selected = prefs.weatherProvider.getValue2()
        return if (selected == OWMWeatherProvider::class.java.name) {
            owmWeatherProvider
        } else {
            openMeteoProvider
        }
    }

    init {
        scope.launch {
            combine(
                prefs.weatherProvider.get(),
                prefs.weatherProvider.get2(),
                prefs.weatherUnit.get(),
                prefs.weatherCity.get(),
                prefs.owmWeatherApiKey.get()
            ) { enabled, provider, unit, city, apiKey ->
                WeatherConfig(enabled, provider, unit, city, apiKey)
            }
                .distinctUntilChanged()
                .collect { config ->
                    if (config.enabled) {
                        refreshWeather(force = true)
                    } else {
                        _weatherState.value = WeatherState.Idle
                    }
                }
        }
    }

    private data class WeatherConfig(
        val enabled: Boolean,
        val provider: String,
        val unit: String,
        val city: String,
        val apiKey: String
    )

    fun refreshWeather(force: Boolean = false) {
        if (!prefs.weatherProvider.getValue()) {
            _weatherState.value = WeatherState.Idle
            return
        }

        val now = System.currentTimeMillis()
        if (!force && _weatherState.value is WeatherState.Success && (now - lastFetchTime < cacheDurationMillis)) {
            return
        }

        scope.launch {
            mutex.withLock {
                if (force || _weatherState.value !is WeatherState.Success) {
                    _weatherState.value = WeatherState.Loading
                }

                try {
                    val activeProvider = getActiveProvider()
                    val customCity = prefs.weatherCity.getValue().trim()

                    var lat: Double
                    var lon: Double
                    var resolvedCityName: String

                    if (customCity.isNotEmpty()) {
                        val geo = activeProvider.getCoordinatesForCity(customCity)
                        if (geo != null) {
                            lat = geo.latitude
                            lon = geo.longitude
                            resolvedCityName = geo.name
                        } else {
                            lat = 13.6989
                            lon = -89.1910
                            resolvedCityName = customCity
                        }
                    } else {
                        val resolvedLoc = locationHelper.getCurrentOrLastLocation()
                        if (resolvedLoc != null) {
                            lat = resolvedLoc.latitude
                            lon = resolvedLoc.longitude
                            resolvedCityName = resolvedLoc.cityName
                        } else {
                            // Default location if location permission is not yet granted or available
                            lat = 13.6989
                            lon = -89.1910
                            resolvedCityName = "San Salvador"
                        }
                    }

                    val weatherData = activeProvider.fetchWeather(
                        latitude = lat,
                        longitude = lon,
                        cityName = resolvedCityName
                    )

                    lastFetchTime = System.currentTimeMillis()
                    _weatherState.value = WeatherState.Success(weatherData)
                } catch (e: Exception) {
                    Log.e("WeatherRepository", "Failed to fetch weather", e)
                    _weatherState.value = WeatherState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }
}
