package com.saulhdev.feeder

import android.app.Activity
import android.os.Bundle
import androidx.multidex.MultiDexApplication
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen
import com.saulhdev.feeder.db.FeedArticleDao
import com.saulhdev.feeder.db.FeedDao
import com.saulhdev.feeder.db.FeedStore
import com.saulhdev.feeder.db.NeoFeedDb
import com.saulhdev.feeder.models.Repository
import com.saulhdev.feeder.plugin.PluginFetcher
import com.saulhdev.feeder.utils.ApplicationCoroutineScope
import com.saulhdev.feeder.utils.OverlayBridge
import com.saulhdev.feeder.utils.Utilities
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.lang.ref.WeakReference

class NFApplication : MultiDexApplication(), DIAware {

    private val activityHandler = ActivityHandler()
    lateinit var db: NeoFeedDb
    private val applicationCoroutineScope = ApplicationCoroutineScope()

    override val di by DI.lazy {

        import(androidXModule(this@NFApplication))
        bind<NeoFeedDb>() with singleton { NeoFeedDb.getInstance(this@NFApplication) }
        bind<WorkManager>() with singleton { WorkManager.getInstance(this@NFApplication) }
        bind<FeedDao>() with singleton { instance<NeoFeedDb>().feedDao() }
        bind<FeedArticleDao>() with singleton { instance<NeoFeedDb>().feedArticleDao() }

        bind<Repository>() with singleton { Repository(di) }
        bind<FeedStore>() with singleton { FeedStore(di) }
        bind<ApplicationCoroutineScope>() with instance(applicationCoroutineScope)

        //bindWithComposableViewModelScope<EditFeedViewModel>()
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        instance = this
        PluginFetcher.init(instance)
        db = NeoFeedDb.getInstance(applicationContext)
    }

    fun restart(recreate: Boolean = true) {
        if (recreate) {
            activityHandler.finishAll(recreate)
        } else {
            Utilities.getInstance().restartFeed(this)
        }
    }

    class ActivityHandler : ActivityLifecycleCallbacks {

        private val activities = HashSet<Activity>()
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

        private var mainActivityRef: WeakReference<MainActivity> = WeakReference(null)
        var mainActivity: MainActivity?
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }
    }
}