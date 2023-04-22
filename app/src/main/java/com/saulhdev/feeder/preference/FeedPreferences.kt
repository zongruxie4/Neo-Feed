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
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Adb
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.ContactEmergency
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Wifi
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.utils.getBackgroundOptions
import com.saulhdev.feeder.utils.getItemsPerFeed
import com.saulhdev.feeder.utils.getSyncFrequency
import com.saulhdev.feeder.utils.getThemes
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

class FeedPreferences(val context: Context) {
    var sharedPrefs: SharedPreferences =
        context.getSharedPreferences("com.saulhdev.neofeed.prefs", Context.MODE_PRIVATE)

    private var doNothing = {}

    private var recreate = {
        NFApplication.instance.restart(true)
    }

    private var restart = {
        NFApplication.instance.restart(false)
    }

    /*=== Appearance ===*/
    var overlayTheme = StringSelectionPref(
        key = "pref_overlay_theme",
        titleId = R.string.pref_ovr_theme,
        defaultValue = "auto_launcher",
        entries = getThemes(context),
        icon = Icons.Outlined.Style,
        onChange = recreate
    )

    var overlayTransparency = FloatPref(
        key = "pref_overlay_opacity",
        titleId = R.string.pref_transparency,
        icon = Icons.Outlined.Brightness4,
        defaultValue = 1f,
        maxValue = 1f,
        minValue = 0f,
        steps = 100,
        specialOutputs = { "${(it * 100).roundToInt()}%" },
        onChange = doNothing
    )

    var systemColors = BooleanPref(
        key = "pref_overlay_system_colors",
        titleId = R.string.pref_syscolors,
        summaryId = R.string.pref_syscolors_desc,
        icon = Icons.Outlined.Wallpaper,
        defaultValue = false,
        onChange = recreate
    )

    var overlayBackground = StringSelectionPref(
        key = "pref_overlay_background",
        titleId = R.string.pref_bg_color,
        defaultValue = "theme",
        entries = getBackgroundOptions(context),
        icon = Icons.Outlined.ContactEmergency,
        onChange = recreate
    )

    var cardBackground = StringSelectionPref(
        key = "pref_overlay_card_background",
        titleId = R.string.pref_card_bg,
        defaultValue = "theme",
        entries = getBackgroundOptions(context),
        icon = Icons.Outlined.Opacity,
        onChange = recreate
    )

    /*=== Sources ===*/
    var sources = StringPref(
        key = "pref_sources",
        titleId = R.string.title_sources,
        summaryId = R.string.summary_sources,
        icon = Icons.Outlined.Dashboard,
        route = "/${Routes.SOURCES}/",
        onChange = doNothing
    )

    var openInBrowser = BooleanPref(
        key = "pref_open_browser",
        titleId = R.string.pref_browser_theme,
        icon = Icons.Outlined.OpenInBrowser,
        defaultValue = false,
        onChange = doNothing
    )

    var offlineReader = BooleanPref(
        key = "pref_offline_reader",
        titleId = R.string.pref_offline_reader,
        icon = Icons.Outlined.Wallpaper,
        defaultValue = true,
        onChange = doNothing
    )

    /*=== Sync ===*/
    var syncOnlyOnWifi = BooleanPref(
        key = "pref_sync_only_wifi",
        titleId = R.string.pref_sync_wifi,
        icon = Icons.Outlined.Wifi,
        defaultValue = true
    )
    var syncFrequency = StringSelectionPref(
        key = "pref_sync_frequency",
        titleId = R.string.pref_sync_frequency,
        defaultValue = "1",
        entries = getSyncFrequency(context),
        icon = Icons.Outlined.AccessTime,
        onChange = doNothing
    )

    var itemsPerFeed = StringSelectionPref(
        key = "pref_items_per_feed",
        titleId = R.string.pref_items_per_feed,
        defaultValue = "25",
        entries = getItemsPerFeed(),
        icon = Icons.Outlined.Feed,
        onChange = doNothing
    )

    /*=== Others ===*/
    var enabledPlugins = StringSetPref(
        key = "pref_enabled_plugins",
        titleId = R.string.title_plugin_list,
        defaultValue = setOf(),
        onChange = doNothing
    )

    var about = StringPref(
        key = "pref_about",
        titleId = R.string.title_about,
        icon = Icons.Outlined.Info,
        route = "/${Routes.ABOUT}/",
        onChange = doNothing
    )

    var debugging = BooleanPref(
        key = "pref_debugging",
        titleId = R.string.debug_logcat_printing,
        defaultValue = false,
        icon = Icons.Outlined.Adb,
        onChange = doNothing
    )

    var aboutLicense = StringPref(
        key = "pref_license",
        titleId = R.string.about_licenses,
        icon = R.drawable.ic_copyright,
        route = Routes.LICENSE,
    )

    var aboutChangelog = StringPref(
        key = "pref_changelog",
        titleId = R.string.about_changelog,
        icon = R.drawable.ic_list,
        route = Routes.CHANGELOG,
    )

    /*HELPER CLASSES FOR PREFERENCES*/
    inner class StringPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        val onClick: (() -> Unit)? = null,
        onChange: () -> Unit = doNothing,
        val route: String = "",
        val url: String = "",
        val icon: Any? = null
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(key, defaultValue)!!

        override fun onSetValue(value: String) {
            edit { putString(key, value) }
        }
    }

    inner class StringSetPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Set<String>,
        val onClick: (() -> Unit)? = null,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Set<String>>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Set<String> = sharedPrefs.getStringSet(getKey(), defaultValue)!!

        override fun onSetValue(value: Set<String>) {
            edit { putStringSet(key, value) }
        }
    }

    open inner class StringSelectionPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: String = "",
        val entries: Map<String, String>,
        val icon: Any? = null,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<String>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): String = sharedPrefs.getString(getKey(), defaultValue)!!
        override fun onSetValue(value: String) {
            edit { putString(getKey(), value) }
        }
    }

    open inner class FloatPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Float = 0f,
        val icon: Any? = null,
        val minValue: Float,
        val maxValue: Float,
        val steps: Int,
        val specialOutputs: ((Float) -> String) = Float::toString,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Float>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Float = sharedPrefs.getFloat(getKey(), defaultValue)

        override fun onSetValue(value: Float) {
            edit { putFloat(getKey(), value) }
        }
    }

    open inner class BooleanPref(
        key: String,
        @StringRes titleId: Int,
        @StringRes summaryId: Int = -1,
        defaultValue: Boolean = false,
        val icon: Any? = null,
        onChange: () -> Unit = doNothing
    ) : PrefDelegate<Boolean>(key, titleId, summaryId, defaultValue, onChange) {
        override fun onGetValue(): Boolean = sharedPrefs.getBoolean(getKey(), defaultValue)

        override fun onSetValue(value: Boolean) {
            edit { putBoolean(getKey(), value) }
        }
    }

    abstract inner class PrefDelegate<T : Any>(
        val key: String,
        @StringRes val titleId: Int,
        @StringRes var summaryId: Int = -1,
        val defaultValue: T,
        private val onChange: () -> Unit
    ) {
        private var cached = false
        private lateinit var value: T

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!cached) {
                value = onGetValue()
                cached = true
            }
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            cached = false
            onSetValue(value)
        }

        abstract fun onGetValue(): T

        abstract fun onSetValue(value: T)

        protected inline fun edit(body: SharedPreferences.Editor.() -> Unit) {
            val editor = sharedPrefs.edit()
            body(editor)
            editor.apply()
        }

        internal fun getKey() = key

        private fun onValueChanged() {
            discardCachedValue()
            onChange.invoke()
        }

        private fun discardCachedValue() {
            if (cached) {
                cached = false
                value.let(::disposeOldValue)
            }
        }

        open fun disposeOldValue(oldValue: T) {}
    }
}