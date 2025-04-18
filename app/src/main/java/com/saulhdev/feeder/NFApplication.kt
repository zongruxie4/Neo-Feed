package com.saulhdev.feeder

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.SavedStateHandle
import androidx.multidex.MultiDexApplication
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.jakewharton.threetenabp.AndroidThreeTen
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.NeoFeedDb
import com.saulhdev.feeder.db.SourceRepository
import com.saulhdev.feeder.plugin.PluginFetcher
import com.saulhdev.feeder.sync.SyncRestClient
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.OverlayBridge
import com.saulhdev.feeder.utils.ToastMaker
import com.saulhdev.feeder.utils.Utilities
import com.saulhdev.feeder.viewmodel.EditFeedViewModel
import com.saulhdev.feeder.viewmodel.SearchFeedViewModel
import com.saulhdev.feeder.viewmodel.SourcesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import java.lang.ref.WeakReference

class NFApplication : MultiDexApplication(), KoinStartup {

    private val activityHandler = ActivityHandler()
    private val applicationCoroutineScope = ApplicationCoroutineScope()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "neo_feed",
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration(context, "com.saulhdev.neofeed.prefs"))
        }
    )
    private val wm: WorkManager by inject(WorkManager::class.java)

    private fun savedStateHandle() = SavedStateHandle()

    private val modelModule = module {
        single {
            savedStateHandle()
        }
        viewModelOf(::EditFeedViewModel)
        viewModelOf(::SearchFeedViewModel)
        viewModelOf(::SourcesViewModel)
    }

    private val dataModule = module {
        single<NeoFeedDb> { NeoFeedDb.getInstance(this@NFApplication) }
        single { androidContext().dataStore }
        single { ArticleRepository(this@NFApplication) }
        single { SourceRepository(this@NFApplication) }
        single { SyncRestClient(this@NFApplication) }
    }

    private val coreModule = module {
        single { contentResolver }
        single { WorkManager.getInstance(this@NFApplication) }
        single<ToastMaker> {
            object : ToastMaker {
                override suspend fun makeToast(text: String) = withContext(Dispatchers.Main) {
                    Toast.makeText(get(), text, Toast.LENGTH_SHORT).show()
                }

                override suspend fun makeToast(resId: Int) = withContext(Dispatchers.Main) {
                    Toast.makeText(get(), resId, Toast.LENGTH_SHORT).show()
                }
            }
        }
        single { applicationCoroutineScope }
        single<NFApplication> { this@NFApplication }
    }

    @KoinExperimentalAPI
    override fun onKoinStartup() = koinConfiguration {
            androidLogger()
            androidContext(this@NFApplication)
            modules(coreModule, dataModule, modelModule)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this)
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> DynamicColors.isDynamicColorAvailable() }
                .build()
        )
        wm.pruneWork()
        PluginFetcher.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        GlobalContext.get().close()
    }

    fun restart(recreate: Boolean = false) {
        if (recreate) {
            activityHandler.finishAll(true)
        } else {
            Utilities().restartApp(this)
        }
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        private val activities = HashSet<Activity>()
        private var foregroundActivity: Activity? = null

        fun finishAll(recreateLauncher: Boolean = true) {
            HashSet(activities).forEach { if (recreateLauncher && it is MainActivity) it.recreate() else it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity)
                foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    companion object {
        lateinit var instance: NFApplication
        val bridge = OverlayBridge()

        private var mainActivityRef: WeakReference<MainActivity> = WeakReference(null)
        var mainActivity: MainActivity?
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }
    }
}