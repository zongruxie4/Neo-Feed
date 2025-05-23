package com.saulhdev.feeder.manager.plugin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.saulhdev.feeder.data.content.FeedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object PluginFetcher : KoinComponent {
    // List of available packages.
    private val availablePlugins = hashMapOf<String, SlimPluginInfo>()
    val prefs: FeedPreferences by inject()

    // Required part for AIDL connection.
    const val INTENT_ACTION_SERVICE = "ua.itaysonlab.hfsdk.HOMEFEEDER_PLUGIN_SERVICE"

    // Metadata value acting for SDK version.
    private const val METADATA_SDK_VERSION = "HF_PluginSDK_Version"

    private const val METADATA_NAME = "HF_Plugin_Name"
    private const val METADATA_DESCRIPTION = "HF_Plugin_Description"
    private const val METADATA_AUTHOR = "HF_Plugin_Author"
    private const val METADATA_HAS_SETTINGS = "HF_Plugin_HasSettingsActivity"

    fun init(ctx: Context) {
        fillListBy(ctx.packageManager)
    }

    // Fill list of suitable packages.
    private fun fillListBy(packageManager: PackageManager) {
        availablePlugins.clear()

        val hasService = packageManager.queryIntentServices(
            Intent(INTENT_ACTION_SERVICE),
            PackageManager.GET_META_DATA
        ).map {
            Pair(it.serviceInfo.packageName, it.serviceInfo.metaData)
        }

        if (prefs.debugging.getValue()) {
            Log.d("PluginFetcher", "Packages that has service: $hasService")
        }

        hasService.forEach {
            //Logger.log("PluginFetcher", "$it")
            availablePlugins[it.first] = SlimPluginInfo(
                it.first,
                hasPluginSettings = it.second.getBoolean(METADATA_HAS_SETTINGS),
                sdkVersion = it.second.getInt(METADATA_SDK_VERSION),
                title = it.second.getString(METADATA_NAME, ""),
                description = it.second.getString(METADATA_DESCRIPTION, ""),
                author = it.second.getString(METADATA_AUTHOR, "")
            )
        }
    }

    data class SlimPluginInfo(
        val pkg: String,
        val hasPluginSettings: Boolean,
        val sdkVersion: Int,
        val title: String,
        val description: String,
        val author: String
    )
}