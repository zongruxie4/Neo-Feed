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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.net.toUri
import androidx.lifecycle.asLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.saulhdev.feeder.compose.navigation.NavigationManager
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sync.FeedSyncer
import com.saulhdev.feeder.theme.AppTheme
import com.saulhdev.feeder.viewmodel.DIAwareComponentActivity
import org.kodein.di.compose.withDI
import java.util.concurrent.TimeUnit

class MainActivity : DIAwareComponentActivity() {
    lateinit var prefs: FeedPreferences
    private var isDarkTheme = false

    private lateinit var navController: NavHostController

    private var sRestart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        NFApplication.mainActivity = this
        super.onCreate(savedInstanceState)

        prefs = FeedPreferences.getInstance(this)
        setContent {
            AppTheme(
                darkTheme = isDarkTheme
            ) {
                withDI {
                    navController = rememberNavController()
                    NavigationManager(navController = navController)
                }
            }
        }
        if (prefs.enabledPlugins.getValue().isEmpty()) {
            val list: ArrayList<String> = ArrayList()
            list.add(BuildConfig.APPLICATION_ID)
            prefs.enabledPlugins.setValue(list.toSet())
        }

        configurePeriodicSync(prefs)
        observePrefs()
    }

    private fun observePrefs() {
        var recreate = false
        prefs.overlayTheme.get().asLiveData().observe(this) {
            isDarkTheme = when (it) {
                "auto_system" -> isSystemDark()
                "dark" -> true
                else -> false
            }
            recreate = true
        }
        prefs.overlayTransparency.get().asLiveData().observe(this) {
            recreate = true
        }
        prefs.cardBackground.get().asLiveData().observe(this) {
            recreate = true
        }

        if (recreate) {
            recreate()
            recreate = false
        }
    }

    private fun isSystemDark(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false
        }
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
        val shouldSync = (prefs.syncFrequency.getValue().toDouble()) > 0
        val replace = true
        if (shouldSync) {
            val constraints = Constraints.Builder()

            if (prefs.syncOnlyOnWifi.getValue()) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }
            val timeInterval = (prefs.syncFrequency.getValue().toDouble() * 60).toLong()

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
                    true -> ExistingPeriodicWorkPolicy.UPDATE
                    false -> ExistingPeriodicWorkPolicy.KEEP
                },
                syncWork
            )

        } else {
            workManager.cancelUniqueWork("feeder_periodic_3")
        }
    }

    companion object {
        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
        }
    }
}