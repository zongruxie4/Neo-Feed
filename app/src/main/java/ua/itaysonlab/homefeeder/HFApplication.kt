package ua.itaysonlab.homefeeder

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toDrawable
import com.saulhdev.feeder.BuildConfig
import ua.itaysonlab.hfsdk.HFPluginApplication
import ua.itaysonlab.homefeeder.pluginsystem.PluginFetcher
import ua.itaysonlab.homefeeder.utils.Logger
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