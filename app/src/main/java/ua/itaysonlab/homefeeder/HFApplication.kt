package ua.itaysonlab.homefeeder

import ua.itaysonlab.hfsdk.HFPluginApplication
import ua.itaysonlab.homefeeder.pluginsystem.PluginFetcher
import ua.itaysonlab.homefeeder.utils.OverlayBridge

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