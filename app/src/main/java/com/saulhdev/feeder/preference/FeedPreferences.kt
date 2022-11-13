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
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.navigation.Routes
import com.saulhdev.feeder.utils.getBackgroundOptions
import com.saulhdev.feeder.utils.getSyncFrecuency
import com.saulhdev.feeder.utils.getThemes
import com.saulhdev.feeder.utils.getTransparencyOptions
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.reflect.KProperty

class FeedPreferences(val context: Context) {
    var sharedPrefs: SharedPreferences =
        context.getSharedPreferences("com.saulhdev.neofeed.prefs", Context.MODE_PRIVATE)

    private var doNothing = {}
    private var recreate = { recreate() }

    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("NeoFeedRepository")

    private fun recreate() {
    }

    /*=== Appearance ===*/
    var overlayTheme = StringSelectionPref(
        key = "pref_overlay_theme",
        titleId = R.string.pref_ovr_theme,
        defaultValue = "auto_launcher",
        entries = getThemes(context),
        icon = R.drawable.ic_style,
        onChange = recreate
    )

    var overlayTransparency = StringSelectionPref(
        key = "pref_overlay_transparency",
        titleId = R.string.pref_transparency,
        defaultValue = "non_transparent",
        entries = getTransparencyOptions(context),
        icon = R.drawable.ic_circle, //TODO: Change icon
        onChange = recreate
    )

    var systemColors = BooleanPref(
        key = "pref_overlay_system_colors",
        titleId = R.string.pref_syscolors,
        summaryId = R.string.pref_syscolors_desc,
        icon = R.drawable.ic_wallpaper,
        defaultValue = false,
        onChange = recreate
    )

    var overlayBackground = StringSelectionPref(
        key = "pref_overlay_background",
        titleId = R.string.pref_bg_color,
        defaultValue = "theme",
        entries = getBackgroundOptions(context),
        icon = R.drawable.ic_circle, //TODO: Change icon
        onChange = recreate
    )

    var cardBackground = StringSelectionPref(
        key = "pref_overlay_card_background",
        titleId = R.string.pref_card_bg,
        defaultValue = "theme",
        entries = getBackgroundOptions(context),
        icon = R.drawable.ic_card,
        onChange = recreate
    )

    /*=== Sources ===*/
    var sources = StringPref(
        key = "pref_sources",
        titleId = R.string.title_sources,
        summaryId = R.string.summary_sources,
        icon = R.drawable.ic_services_outline_28,
        route = "/${Routes.SOURCES}/",
        onChange = doNothing
    )

    var openInBrowser = BooleanPref(
        key = "pref_open_browser",
        titleId = R.string.pref_browser_theme,
        defaultValue = false,
        onChange = recreate
    )


    /*=== Sync ===*/
    var syncOnlyOnWifi = BooleanPref(
        key = "pref_sync_only_wifi",
        titleId = R.string.pref_sync_wifi,
        defaultValue = true,
        onChange = {
            scope.launch {
            }
        }
    )
    var syncFrequency = StringSelectionPref(
        key = "pref_sync_frequency",
        titleId = R.string.pref_sync_frequency,
        defaultValue = "1",
        entries = getSyncFrecuency(context),
        icon = R.drawable.ic_clock,
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
        icon = R.drawable.ic_info_outline_28,
        route = "/${Routes.ABOUT}/",
        onChange = doNothing
    )

    var debugging = BooleanPref(
        key = "pref_debugging",
        titleId = R.string.debug_logcat_printing,
        defaultValue = false,
        icon = R.drawable.ic_debug,
        onChange = doNothing
    )

    var developer1 = StringPref(
        key = "pref_developer",
        titleId = R.string.pref_tg_author,
        summaryId = R.string.about_developer,
        icon = "https://avatars.githubusercontent.com/u/6044050",
        url = "https://github.com/saulhdev",
        onChange = doNothing
    )

    var developer2 = StringPref(
        key = "pref_developer",
        titleId = R.string.pref_tg_author,
        summaryId = R.string.about_developer2,
        icon = "https://avatars.githubusercontent.com/u/40302595",
        url = "https://github.com/machiav3lli",
        onChange = doNothing
    )

    var telegramChannel = StringPref(
        key = "pref_channel",
        titleId = R.string.pref_tg,
        summaryId = R.string.telegram_channel,
        icon = R.drawable.ic_telegram,
        url = "https://t.me/neo_launcher",
        onChange = doNothing
    )

    var sourceCode = StringPref(
        key = "pref_source_code",
        titleId = R.string.pref_git,
        summaryId = R.string.source_code_url,
        icon = R.drawable.ic_code,
        url = context.getString(R.string.source_code_url),
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