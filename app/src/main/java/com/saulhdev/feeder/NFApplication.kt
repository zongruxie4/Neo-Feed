package com.saulhdev.feeder

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen
import com.saulhdev.feeder.plugin.PluginFetcher
import com.saulhdev.feeder.utils.OverlayBridge
import com.saulhdev.feeder.utils.Utilities
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.singleton
import ua.itaysonlab.hfsdk.HFPluginApplication

class NFApplication : HFPluginApplication(), DIAware {

    private val activityHandler = ActivityHandler()

    override val di by DI.lazy {
        bind<Application>() with singleton { this@NFApplication }
        bind<WorkManager>() with singleton { WorkManager.getInstance(this@NFApplication) }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        instance = this
        PluginFetcher.init(instance)
    }

    fun restart(recreate: Boolean = true) {
        if (recreate) {
            activityHandler.finishAll(recreate)
        } else {
            Utilities.getInstance().restartFeed(this)
        }
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        val activities = HashSet<Activity>()
        var foregroundActivity: Activity? = null

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
    }
}