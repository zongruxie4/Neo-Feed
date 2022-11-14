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

package com.saulhdev.feeder

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavHostController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saulhdev.feeder.compose.navigation.NavigationManager
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sync.FeedSyncer
import com.saulhdev.feeder.theme.AppTheme
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var prefs: FeedPreferences
    private val prefsToWatch = arrayOf(
        "pref_overlay_theme",
        "pref_overlay_transparency",
        "pref_overlay_system_colors",
        "pref_overlay_background",
        "pref_overlay_card_background"
    )

    private lateinit var navController: NavHostController

    private var sRestart = false

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                navController = rememberAnimatedNavController()
                NavigationManager(navController = navController)
            }
        }
        prefs = FeedPreferences(this)
        if (prefs.enabledPlugins.onGetValue().isEmpty()) {
            val list: ArrayList<String> = ArrayList()
            list.add(BuildConfig.APPLICATION_ID)
            prefs.enabledPlugins.onSetValue(list.toSet())
        }

        configurePeriodicSync(prefs)
    }

    override fun onRestart() {
        super.onRestart()

        restartIfPending()
    }

    private fun restartIfPending() {
        if (sRestart) {
            NFApplication.instance.restart(false)
        }
    }

    private fun configurePeriodicSync(prefs: FeedPreferences) {
        val workManager = WorkManager.getInstance(this)
        val shouldSync = (prefs.syncFrequency.onGetValue().toDouble()) > 0
        val replace = true
        if (shouldSync) {
            val constraints = Constraints.Builder()

            if (prefs.syncOnlyOnWifi.onGetValue()) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }
            val timeInterval = (prefs.syncFrequency.onGetValue().toDouble() * 60).toLong()

            val workRequestBuilder = PeriodicWorkRequestBuilder<FeedSyncer>(
                timeInterval,
                TimeUnit.MINUTES,
            )

            val syncWork = workRequestBuilder
                .setConstraints(constraints.build())
                .addTag("feeder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "feeder_periodic_3",
                when (replace) {
                    true -> ExistingPeriodicWorkPolicy.REPLACE
                    false -> ExistingPeriodicWorkPolicy.KEEP
                },
                syncWork
            )

        } else {
            workManager.cancelUniqueWork("feeder_periodic_3")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.sharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (prefsToWatch.contains(key)) {
            recreate()
        }
    }
}