package ua.itaysonlab.homefeeder

import com.saulhdev.feeder.utils.OverlayBridge
import ua.itaysonlab.hfsdk.HFPluginApplication
import ua.itaysonlab.homefeeder.pluginsystem.PluginFetcher

class HFApplication : HFPluginApplication() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        PluginFetcher.init(instance)
    }

    companion object {

        lateinit var instance: HFApplication
        val bridge = OverlayBridge()
    }
}