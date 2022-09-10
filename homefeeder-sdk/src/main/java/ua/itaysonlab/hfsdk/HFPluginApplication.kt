package ua.itaysonlab.hfsdk

import android.app.Application

open class HFPluginApplication: Application() {
    companion object {
        lateinit var instance: HFPluginApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}