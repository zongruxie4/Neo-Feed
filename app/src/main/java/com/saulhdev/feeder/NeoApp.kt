package com.saulhdev.feeder

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import com.saulhdev.feeder.data.content.FeedPreferences.Companion.prefsModule
import com.saulhdev.feeder.service.OverlayBridge
import com.saulhdev.feeder.utils.Utilities
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.GlobalContext
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
class NeoApp: MultiDexApplication(), KoinStartup {
    val activityHandler = ActivityHandler()
    private val coreModule = module {}
    private val dataModule = module {}
    private val modelModule = module {}

    fun onAppStarted() {
        registerActivityLifecycleCallbacks(activityHandler)
    }

    @KoinExperimentalAPI
    override fun onKoinStartup() = koinConfiguration {
        androidLogger()
        androidContext(this@NeoApp)
        modules(coreModule, prefsModule, dataModule, modelModule)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        onAppStarted()
    }

    override fun onTerminate() {
        super.onTerminate()
        GlobalContext.get().close()
    }

    fun onRestart(recreate: Boolean = true){
        if (recreate) {
            activityHandler.finishAll(recreateApp = false)
        } else {
            Utilities().restartApp(this)
        }
    }

    companion object {
        private const val TAG = "NeoFeed"
        @JvmStatic
        var instance: NeoApp? = null
            private set

        val bridge = OverlayBridge()
    }
}

class ActivityHandler : ActivityLifecycleCallbacks {
    val activities = HashSet<Activity>()
    var foregroundActivity: Activity? = null

    fun finishAll(recreateApp: Boolean = true) {
        HashSet(activities).forEach { if (recreateApp) it.recreate() else it.finish() }
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