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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.multidex.BuildConfig
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.manager.models.exportOpml
import com.saulhdev.feeder.manager.models.importOpml
import com.saulhdev.feeder.manager.sync.FeedSyncer
import com.saulhdev.feeder.ui.compose.theme.AppTheme
import com.saulhdev.feeder.ui.navigation.NAV_BASE
import com.saulhdev.feeder.ui.navigation.NavigationManager
import com.saulhdev.feeder.utils.extensions.isDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class MainActivity : ComponentActivity(), SavedStateRegistryOwner {
    val prefs: FeedPreferences by inject()
    private lateinit var navController: NavHostController

    private var sRestart = false

    private val localTime = LocalDateTime.now().toString().replace(":", "_").substring(0, 19)
    private val opmlImporterLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (pendingOperation) {
                        DocumentOperation.IMPORT -> importOpml(uri)
                        else                     -> {} // No relevant operation pending
                    }
                }
            }
        }

    private val opmlExporter = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/xml")
    ) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                when (pendingOperation) {
                    DocumentOperation.EXPORT -> exportOpml(uri)
                    else                     -> {} // No relevant operation pending
                }
            }
        }
    }

    private enum class DocumentOperation { IMPORT, EXPORT }

    private var pendingOperation: DocumentOperation? = null

    private fun launchOpmlImporter() {
        pendingOperation = DocumentOperation.IMPORT
        opmlImporterLauncher.launch(arrayOf("application/xml", "text/xml", "text/opml"))
    }

    private fun launchOpmlExporter() {
        pendingOperation = DocumentOperation.EXPORT
        opmlExporter.launch("NF-${localTime}.opml")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        when {
            intent.hasExtra("import") -> launchOpmlImporter()
            intent.hasExtra("export") -> launchOpmlExporter()
        }

        setContent {
            navController = rememberNavController()
            TransparentSystemBars()

            AppTheme(
                darkTheme = when (prefs.overlayTheme.getValue()) {
                    "auto_system" -> isSystemInDarkTheme()
                    else          -> isDarkTheme
                }
            ) {
                NavigationManager(navController = navController)
            }
        }
        if (prefs.enabledPlugins.getValue().isEmpty()) {
            val list: ArrayList<String> = ArrayList()
            list.add(BuildConfig.APPLICATION_ID)
            prefs.enabledPlugins.setValue(list.toSet())
        }

        configurePeriodicSync()
        observePrefs()
        NeoApp.mainActivity = this
    }

    @Composable
    fun TransparentSystemBars() {
        DisposableEffect(isDarkTheme, prefs.overlayTheme.getValue()) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
            )
            onDispose {}
        }
    }

    private fun observePrefs() {
        val oldTheme = prefs.overlayTheme.getValue()
        val oldTransparency = prefs.overlayTransparency.getValue()
        //val oldCardBackground = prefs.cardBackground.getValue()

        prefs.overlayTheme.get().asLiveData().observe(this) {
            if (it != oldTheme) {
                recreate()
            }
        }
        prefs.overlayTransparency.get().asLiveData().observe(this) {
            if (it != oldTransparency) {
                recreate()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        restartIfPending()
    }

    private fun restartIfPending() {
        if (sRestart) {
            NeoApp.instance.restart(false)
        }
    }

    private fun configurePeriodicSync() {
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
                .addTag("PeriodicFeedSyncer")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "feeder_periodic_3",
                when (replace) {
                    true  -> ExistingPeriodicWorkPolicy.UPDATE
                    false -> ExistingPeriodicWorkPolicy.KEEP
                },
                syncWork
            )

        } else {
            workManager.cancelUniqueWork("feeder_periodic_3")
        }
    }

    companion object {
        fun navigateIntent(context: Context, destination: String): Intent {
            val uri = "$NAV_BASE$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
        }

        private suspend fun start(
            activity: Activity,
            targetIntent: Intent,
            extras: Bundle
        ): ActivityResult {
            return suspendCancellableCoroutine { continuation ->
                val intent = Intent(activity, MainActivity::class.java)
                    .putExtras(extras)
                    .putExtra("intent", targetIntent)
                val resultReceiver = createResultReceiver {
                    if (continuation.isActive) {
                        continuation.resume(it)
                    }
                }
                activity.startActivity(intent.putExtra("callback", resultReceiver))
            }
        }

        private fun createResultReceiver(callback: (ActivityResult) -> Unit): ResultReceiver {
            return object : ResultReceiver(Handler(Looper.myLooper()!!)) {

                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    val data = Intent()
                    if (resultData != null) {
                        data.putExtras(resultData)
                    }
                    callback(ActivityResult(resultCode, data))
                }
            }
        }
    }
}