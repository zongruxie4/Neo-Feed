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

import android.content.Context
import android.util.Log
import com.saulhdev.feeder.R
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
import kotlinx.serialization.json.Json

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
    private val cacheDurationMillis = 60 * 60 * 1000L // 1 hour

    private val sharedPrefs =
        prefs.context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    fun getActiveProvider(): WeatherProvider {
        val selected = prefs.weatherProvider.getValue2()
        return if (selected == OWMWeatherProvider::class.java.name) {
            owmWeatherProvider
        } else {
            openMeteoProvider
        }
    }

    private var isInitialEmission = true

    init {
        loadCachedWeather()

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
                    if (isInitialEmission) {
                        isInitialEmission = false
                        if (!config.enabled) {
                            _weatherState.value = WeatherState.Idle
                        }
                    } else {
                        if (config.enabled) {
                            refreshWeather(force = true)
                        } else {
                            _weatherState.value = WeatherState.Idle
                        }
                    }
                }
        }
    }

    private fun loadCachedWeather() {
        if (!prefs.weatherProvider.getValue()) return
        val currentCity = prefs.weatherCity.getValue().trim()
        if (currentCity.isEmpty() && !locationHelper.hasLocationPermission()) {
            _weatherState.value = WeatherState.LocationPermissionRequired
            return
        }

        try {
            val cachedCity = sharedPrefs.getString("cached_city", null)
            if (cachedCity != currentCity) {
                return
            }

            lastFetchTime = sharedPrefs.getLong("last_fetch_time", 0L)
            val json = sharedPrefs.getString("cached_weather", null)
            if (!json.isNullOrEmpty()) {
                val cached = jsonSerializer.decodeFromString<WeatherData>(json)
                _weatherState.value = WeatherState.Success(cached)
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to restore cached weather", e)
        }
    }

    private fun saveCachedWeather(weatherData: WeatherData, fetchTime: Long, cityKey: String) {
        try {
            val json = jsonSerializer.encodeToString(weatherData)
            sharedPrefs.edit()
                .putLong("last_fetch_time", fetchTime)
                .putString("cached_weather", json)
                .putString("cached_city", cityKey)
                .apply()
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to save cached weather", e)
        }
    }

    private var lastFetchAttemptTime = 0L
    private val minFetchIntervalMillis = 5 * 60 * 1000L // 5 minutes retry cooldown

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

        val customCity = prefs.weatherCity.getValue().trim()
        if (customCity.isEmpty() && !locationHelper.hasLocationPermission()) {
            _weatherState.value = WeatherState.LocationPermissionRequired
            return
        }

        val now = System.currentTimeMillis()
        if (_weatherState.value is WeatherState.Loading) {
            return
        }

        if (!force) {
            if (_weatherState.value is WeatherState.Success && (now - lastFetchTime < cacheDurationMillis)) {
                return
            }
            if (now - lastFetchAttemptTime < minFetchIntervalMillis) {
                return
            }
        }

        scope.launch {
            mutex.withLock {
                val currentNow = System.currentTimeMillis()
                if (!force) {
                    if (_weatherState.value is WeatherState.Success && (currentNow - lastFetchTime < cacheDurationMillis)) {
                        return@launch
                    }
                    if (currentNow - lastFetchAttemptTime < minFetchIntervalMillis) {
                        return@launch
                    }
                }

                lastFetchAttemptTime = currentNow

                if (force || _weatherState.value !is WeatherState.Success) {
                    _weatherState.value = WeatherState.Loading
                }

                try {
                    val activeProvider = getActiveProvider()

                    val lat: Double
                    val lon: Double
                    val resolvedCityName: String

                    if (customCity.isNotEmpty()) {
                        val geo = activeProvider.getCoordinatesForCity(customCity)
                        if (geo != null) {
                            lat = geo.latitude
                            lon = geo.longitude
                            resolvedCityName = geo.name
                        } else {
                            _weatherState.value = WeatherState.Error(
                                prefs.context.getString(R.string.weather_error)
                            )
                            return@withLock
                        }
                    } else {
                        if (!locationHelper.hasLocationPermission()) {
                            _weatherState.value = WeatherState.LocationPermissionRequired
                            return@withLock
                        }

                        if (!locationHelper.isLocationEnabled()) {
                            _weatherState.value = WeatherState.Error(
                                prefs.context.getString(R.string.weather_location_disabled)
                            )
                            return@withLock
                        }

                        val resolvedLoc = locationHelper.getCurrentOrLastLocation()
                        if (resolvedLoc != null) {
                            lat = resolvedLoc.latitude
                            lon = resolvedLoc.longitude
                            resolvedCityName = resolvedLoc.cityName
                        } else {
                            _weatherState.value = WeatherState.Error(
                                prefs.context.getString(R.string.weather_error)
                            )
                            return@withLock
                        }
                    }

                    val weatherData = activeProvider.fetchWeather(
                        latitude = lat,
                        longitude = lon,
                        cityName = resolvedCityName
                    )

                    lastFetchTime = System.currentTimeMillis()
                    _weatherState.value = WeatherState.Success(weatherData)
                    saveCachedWeather(weatherData, lastFetchTime, customCity)
                } catch (e: Exception) {
                    Log.e("WeatherRepository", "Failed to fetch weather", e)
                    _weatherState.value = WeatherState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }
}
