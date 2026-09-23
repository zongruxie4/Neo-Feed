/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Neo Feed Team
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
package com.saulhdev.feeder.data.content

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.saulhdev.feeder.data.entity.NeoTheme
import com.saulhdev.feeder.ui.navigation.NavRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.serialization.json.Json

class StringPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<String>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: String = "",
    var onClick: (() -> Unit)? = null,
    val route: NavRoute? = null,
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue)


class TwoStatePref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    val icon: ImageVector,
    key1: Preferences.Key<Boolean>,
    val key2: Preferences.Key<String>,
    val defaultValue1: Boolean = false,
    val defaultValue2: String = "",
    val entries: Map<String, String>
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key1, defaultValue1) {

    fun getValue2(): String {
        return runBlocking(Dispatchers.IO) {
            get2().firstOrNull() ?: defaultValue2
        }
    }

    suspend fun setValue2(value: String) {
        dataStore.edit { it[key2] = value }
    }

    fun get2(): Flow<String> {
        return dataStore.data.map { it[key2] ?: defaultValue2 }
    }

    @Composable
    fun getState2(): State<String> {
        return get2().collectAsState(initial = defaultValue2)
    }
}

open class StringTextPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<String>,
    val defaultValue: String = "",
    val icon: ImageVector,
    onChange: (String) -> Unit = {}
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue)

class StringSelectionPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<String>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: String = "",
    val entries: Map<String, String>
) : PrefDelegate<String>(titleId, summaryId, dataStore, key, defaultValue)

class StringSetPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Set<String>>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Set<String> = emptySet(),
    val route: NavRoute? = null,
    val onClick: (() -> Unit)? = null,
) : PrefDelegate<Set<String>>(titleId, summaryId, dataStore, key, defaultValue)

class BooleanPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Boolean>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Boolean = false,
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue)

class FloatPref(
    @StringRes titleId: Int,
    @StringRes summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<Float>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: Float = 0f,
    val minValue: Float,
    val maxValue: Float,
    val steps: Int,
    val specialOutputs: ((Float) -> String) = Float::toString,
) : PrefDelegate<Float>(titleId, summaryId, dataStore, key, defaultValue)

class AppThemePref(
    @StringRes val titleId: Int,
    @StringRes val summaryId: Int = -1,
    val icon: ImageVector,
    val key: Preferences.Key<String>,
    val dataStore: DataStore<Preferences>,
    val defaultValue: NeoTheme = NeoTheme.DynamicSystem,
) {
    fun getValue(): NeoTheme {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    fun setValue(value: NeoTheme) {
        return runBlocking(Dispatchers.IO) {
            dataStore.edit { it[key] = Json.encodeToString(NeoTheme.serializer(), value) }
        }
    }

    fun get(): Flow<NeoTheme> {
        return dataStore.data.map {
            val str = it[key]
            if (str != null) {
                try {
                    Json.decodeFromString(NeoTheme.serializer(), str)
                } catch (_: Exception) {
                    defaultValue
                }
            } else {
                defaultValue
            }
        }
    }

    @Composable
    fun getState(): State<NeoTheme> {
        return get().collectAsState(initial = defaultValue)
    }
}

abstract class PrefDelegate<T>(
    @StringRes var titleId: Int,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    fun getValue(): T {
        return runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    fun setValue(value: T) {
        return runBlocking(Dispatchers.IO) {
            set(value)
        }
    }

    @Composable
    fun getState(): State<T> {
        return get().collectAsState(initial = defaultValue)
    }

    fun get(): Flow<T> {
        return dataStore.data.map { it[key] ?: defaultValue }
    }

    private suspend fun set(value: T) {
        dataStore.edit { it[key] = value }
    }
}
