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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.weather.WeatherRepository
import com.saulhdev.feeder.manager.localrss.FeedSyncer
import com.saulhdev.feeder.ui.navigation.NAV_BASE
import com.saulhdev.feeder.ui.navigation.NavRoute
import com.saulhdev.feeder.ui.navigation.NavigationManager
import com.saulhdev.feeder.ui.theme.AppTheme
import com.saulhdev.feeder.utils.extensions.isDarkTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)
    private val weatherRepo: WeatherRepository by inject(WeatherRepository::class.java)

    override fun onResume() {
        super.onResume()
        weatherRepo.refreshWeather(force = false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            navController = rememberNavController()
            TransparentSystemBars()
            AppTheme(prefs.appTheme.getValue()) {
                NavigationManager(
                    modifier = Modifier.imePadding(),
                    navController = navController,
                )
            }
        }

        configurePeriodicSync()
        observePrefs()
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent == null) return
        if (::navController.isInitialized) {
            val data = intent.data
            if (data?.scheme == "nf-mastodon" && data.host == "callback") {
                val code = data.getQueryParameter("code").orEmpty()
                val state = data.getQueryParameter("state").orEmpty()
                navController.navigate(NavRoute.MastodonCallback(code = code, state = state))
            } else {
                navController.handleDeepLink(intent)
            }
        }
    }

    @Composable
    fun TransparentSystemBars() {
        DisposableEffect(isDarkTheme, prefs.appTheme.getValue()) {
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
        val oldTheme = prefs.appTheme.getValue()
        val oldTransparency = prefs.overlayTransparency.getValue()

        prefs.appTheme.get().asLiveData().observe(this) {
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

    private fun configurePeriodicSync() {
        val workManager = WorkManager.getInstance(this)
        val freqHours = prefs.syncFrequency.getValue().toDoubleOrNull() ?: 1.0
        val shouldSync = freqHours > 0
        val replace = true
        if (shouldSync) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)

            if (prefs.syncOnlyOnWifi.getValue()) {
                constraints.setRequiredNetworkType(NetworkType.UNMETERED)
            } else {
                constraints.setRequiredNetworkType(NetworkType.CONNECTED)
            }
            val timeInterval = maxOf(15L, (freqHours * 60).toLong())

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