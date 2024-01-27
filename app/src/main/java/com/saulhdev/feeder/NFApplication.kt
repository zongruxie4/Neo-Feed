package com.saulhdev.feeder

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
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
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import java.lang.ref.WeakReference

class NFApplication : MultiDexApplication() {

    private val activityHandler = ActivityHandler()
    lateinit var db: NeoFeedDb
    private val applicationCoroutineScope = ApplicationCoroutineScope()

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
        single { ArticleRepository(this@NFApplication) }
        single { SourceRepository(this@NFApplication) }
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
        single<Application> { this@NFApplication }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        AndroidThreeTen.init(this)
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@NFApplication)
            modules(coreModule, dataModule, modelModule)
        }
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> DynamicColors.isDynamicColorAvailable() }
                .build()
        )
        PluginFetcher.init(this)
        db = NeoFeedDb.getInstance(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        GlobalContext.get().close()
    }

    fun restart(recreate: Boolean = false) {
        if (recreate) {
            activityHandler.finishAll(recreate)
        } else {
            Utilities().restartFeed(this)
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