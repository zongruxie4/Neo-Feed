package ua.itaysonlab.homefeeder.pluginsystem

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.saulhdev.feeder.preference.FeedPreferences
import ua.itaysonlab.hfsdk.FeedCategory
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.IFeedInterface
import ua.itaysonlab.hfsdk.IFeedInterfaceCallback
import ua.itaysonlab.homefeeder.HFApplication

object PluginConnector {
    const val TAG = "PluginConnector"
    private var hasInitialized = false

    private var interfaces = hashMapOf<String, IFeedInterface?>()
    private var callbacks = hashMapOf<String, IFeedInterfaceCallback?>()

    private var index = 0
    private var serviceSize = 0

    private val handler = Handler(Looper.getMainLooper())

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(
                TAG,
                "Connected to service ${componentName.packageName} / ${componentName.className}"
            )
            interfaces[componentName.packageName] = IFeedInterface.Stub.asInterface(iBinder)
            interfaces[componentName.packageName]!!.getFeed(callbacks[componentName.packageName], 0, "default", null)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(
                TAG,
                "Disconnected from service ${componentName.packageName} / ${componentName.className}"
            )
            interfaces[componentName.packageName] = null
        }
    }

    private fun connectTo(pkg: String) {
        Log.d(TAG, "Connecting to: $pkg")
        val intent = Intent("$pkg.HFPluginService")
        intent.action = PluginFetcher.INTENT_ACTION_SERVICE
        intent.setPackage(pkg)
        HFApplication.instance.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
    }

    fun clear() {
        index = 0
        serviceSize = 0
        interfaces.clear()
        callbacks.clear()
        hasInitialized = false
    }

    fun getFeedAsItLoads(page: Int, onNewFeed: (List<FeedItem>) -> Unit, onLoadFinished: () -> Unit) {
        Log.d(TAG, "getFeedAsItLoads")

        val stub = object: IFeedInterfaceCallback.Stub() {
            override fun onCategoriesReceive(categories: MutableList<FeedCategory>?) {

            }

            override fun onFeedReceive(feed: MutableList<FeedItem>?) {
                feed ?: return

                Log.d(TAG, "Received feed: $feed")

                onNewFeed(feed)

                index++
                if (index >= serviceSize) {
                    Log.d(TAG, "Finished chain!")
                    handler.post {
                        onLoadFinished()
                    }
                }
            }
        }

        if (hasInitialized) {
            index = 0
            interfaces.forEach {
                it.value?.getFeed(stub, page, "default", null)
            }
            return
        }

        chainLoad(stub)
        hasInitialized = true
    }

    private fun chainLoad(cb: IFeedInterfaceCallback) {
        index = 0
        val prefs = FeedPreferences(HFApplication.instance)
        serviceSize = prefs.enabledPlugins.onGetValue().size
        prefs.enabledPlugins.onGetValue().forEach {
            callbacks[it] = cb
            connectTo(it)
        }
    }
}