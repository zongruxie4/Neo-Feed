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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.saulhdev.feeder.manager.models.exportOpml
import com.saulhdev.feeder.manager.models.importOpml
import com.saulhdev.feeder.manager.sync.FeedSyncer
import com.saulhdev.feeder.ui.navigation.NAV_BASE
import com.saulhdev.feeder.ui.navigation.NavigationManager
import com.saulhdev.feeder.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.java.KoinJavaComponent.inject
import org.threeten.bp.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class MainActivity : ComponentActivity(){
    private lateinit var navController: NavHostController
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)

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
            AppTheme (
                dynamicColor = prefs.dynamicColor.getValue(),
            ){
                NavigationManager(navController = navController)
            }
        }

        configurePeriodicSync()
        observePrefs()
    }

    @Composable
    fun TransparentSystemBars() {
        //TODO: get key 2 and key 1 from preferences
        val isDarkTheme = false // Replace with actual preference retrieval logic
        DisposableEffect(isDarkTheme, false) {
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
        val dynamicColor = prefs.dynamicColor.getValue()

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
        prefs.dynamicColor.get().asLiveData().observe(this) {
            if (it != dynamicColor) {
                recreate()
            }
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