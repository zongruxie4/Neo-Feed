/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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
package com.saulhdev.feeder.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.navigation.NavRoute
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.BookBookmark
import com.saulhdev.feeder.icon.phosphor.Bookmarks
import com.saulhdev.feeder.icon.phosphor.Browser
import com.saulhdev.feeder.icon.phosphor.Bug
import com.saulhdev.feeder.icon.phosphor.Clock
import com.saulhdev.feeder.icon.phosphor.FunnelSimple
import com.saulhdev.feeder.icon.phosphor.Hash
import com.saulhdev.feeder.icon.phosphor.Info
import com.saulhdev.feeder.icon.phosphor.PaintRoller
import com.saulhdev.feeder.icon.phosphor.SubtractSquare
import com.saulhdev.feeder.icon.phosphor.WifiHigh
import com.saulhdev.feeder.utils.getItemsPerFeed
import com.saulhdev.feeder.utils.getSyncFrequency
import com.saulhdev.feeder.utils.getThemes
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class FeedPreferences private constructor(val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "neo_feed",
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration(context, "com.saulhdev.neofeed.prefs"))
        }
    )
    private val dataStore: DataStore<Preferences> = context.dataStore

    /* Theme */
    var overlayTheme = StringSelectionPref(
        titleId = R.string.pref_ovr_theme,
        icon = Phosphor.PaintRoller,
        key = OVERLAY_THEME,
        dataStore = dataStore,
        defaultValue = "auto_system",
        entries = getThemes(context)
    )

    var overlayTransparency = FloatPref(
        titleId = R.string.pref_transparency,
        icon = Phosphor.SubtractSquare,
        key = OVERLAY_OPACITY,
        dataStore = dataStore,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" }
    )

    /*var cardBackground = StringSelectionPref(
        titleId = R.string.pref_card_bg,
        icon = Phosphor.SelectionBackground,
        key = OVERLAY_CARD_BACKGROUND,
        dataStore = dataStore,
        defaultValue = "theme",
        entries = getBackgroundOptions(context)
    )*/

    var bookmarks = StringPref(
        titleId = R.string.title_bookmarks,
        summaryId = R.string.summary_bookmarks,
        key = BOOKMARKS,
        dataStore = dataStore,
        icon = Phosphor.Bookmarks,
        route = NavRoute.Bookmarks
    )

    var openInBrowser = BooleanPref(
        titleId = R.string.pref_browser_theme,
        icon = Phosphor.Browser,
        key = OPEN_IN_BROWSER,
        dataStore = dataStore,
        defaultValue = false
    )

    var removeDuplicates = BooleanPref(
        titleId = R.string.pref_remove_duplicates,
        icon = Phosphor.FunnelSimple,
        key = REMOVE_DUPLICATES,
        dataStore = dataStore,
        defaultValue = true
    )

    var offlineReader = BooleanPref(
        titleId = R.string.pref_offline_reader,
        icon = Phosphor.BookBookmark,
        key = OFFLINE_READER,
        dataStore = dataStore,
        defaultValue = true
    )

    /* Sync */
    var syncOnlyOnWifi = BooleanPref(
        titleId = R.string.pref_sync_wifi,
        icon = Phosphor.WifiHigh,
        key = SYNC_ON_WIFI,
        dataStore = dataStore,
        defaultValue = true
    )

    var syncFrequency = StringSelectionPref(
        titleId = R.string.pref_sync_frequency,
        icon = Phosphor.Clock,
        key = SYNC_FREQUENCY,
        dataStore = dataStore,
        defaultValue = "1",
        entries = getSyncFrequency(context)
    )

    var itemsPerFeed = StringSelectionPref(
        titleId = R.string.pref_items_per_feed,
        icon = Phosphor.Hash,
        key = ITEMS_PER_FEED,
        dataStore = dataStore,
        defaultValue = "25",
        entries = getItemsPerFeed()
    )

    /* Others */
    var enabledPlugins = StringSetPref(
        titleId = R.string.title_plugin_list,
        icon = Phosphor.Hash,
        key = PLUGINS,
        dataStore = dataStore,
        defaultValue = setOf()
    )

    var overlay = StringPref(
        titleId = R.string.app_name,
        icon = Phosphor.Info,
        key = OVERLAY,
        dataStore = dataStore,
        route = "/${Routes.OVERLAY}/"
    )

    var about = StringPref(
        titleId = R.string.title_about,
        icon = Phosphor.Info,
        key = ABOUT,
        dataStore = dataStore,
        route = NavRoute.About
    )

    var debugging = BooleanPref(
        titleId = R.string.debug_logcat_printing,
        defaultValue = false,
        icon = Phosphor.Bug,
        key = DEBUG,
        dataStore = dataStore,
    )

    companion object {
        private var instance: WeakReference<FeedPreferences>? = null

        @JvmStatic
        fun getInstance(context: Context): FeedPreferences {
            if (instance == null || instance?.get() == null) {
                instance = WeakReference(FeedPreferences(context))
            }
            return instance!!.get()!!
        }

        val OVERLAY_THEME = stringPreferencesKey("pref_overlay_theme")
        val OVERLAY_DYNAMIC_THEME = booleanPreferencesKey("pref_dynamic_theme")
        val OVERLAY_OPACITY = floatPreferencesKey("pref_overlay_opacity")
        val OVERLAY_CARD_BACKGROUND = stringPreferencesKey("pref_overlay_card_background")
        val SOURCES = stringPreferencesKey("pref_sources")
        val BOOKMARKS = stringPreferencesKey("pref_bookmarks")
        val OPEN_IN_BROWSER = booleanPreferencesKey("pref_open_browser")
        val REMOVE_DUPLICATES = booleanPreferencesKey("pref_remove_duplicates")
        val OFFLINE_READER = booleanPreferencesKey("pref_offline_reader")
        val SYNC_ON_WIFI = booleanPreferencesKey("pref_sync_only_wifi")
        val SYNC_FREQUENCY = stringPreferencesKey("pref_sync_frequency")
        val ITEMS_PER_FEED = stringPreferencesKey("pref_items_per_feed")
        val PLUGINS = stringSetPreferencesKey("pref_enabled_plugins")
        val OVERLAY = stringPreferencesKey("pref_overlay")
        val ABOUT = stringPreferencesKey("pref_about")
        val DEBUG = booleanPreferencesKey("pref_debugging")
    }
}