package com.saulhdev.feeder.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.preference.FeedPreferences
import kotlin.system.exitProcess

class Utilities {
    fun restartApp(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        exitProcess(0)
    }

    private fun restartFeed(context: Context) {
        val pm: PackageManager = context.packageManager
        var intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val componentName: ComponentName = intent.resolveActivity(pm)
        if (context.packageName != componentName.packageName) {
            intent = pm.getLaunchIntentForPackage(context.packageName)!!
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        context.startActivity(intent)
        Log.d(this.javaClass.name, "restartFeed: $intent")

        // Create a pending intent so the application is restarted after System.exit(0) was called.
        // We use an AlarmManager to call this intent in 100ms
        val mPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent

        // Kill the application
        exitProcess(0)
    }
}

fun Context.setCustomTheme() {
    AppCompatDelegate.setDefaultNightMode(nightMode)
    if (!(isDynamicTheme && DynamicColors.isDynamicColorAvailable())) {
        setTheme(R.style.AppTheme)
    }
}

val Context.isDynamicTheme
    get() = listOf("auto_system", "auto_system_black")
        .contains(FeedPreferences.getInstance(this).overlayTheme.getValue())

val Context.nightMode
    get() = when (FeedPreferences.getInstance(this).overlayTheme.getValue()) {
        "light"         -> AppCompatDelegate.MODE_NIGHT_NO
        "dark", "black" -> AppCompatDelegate.MODE_NIGHT_YES
        else            -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }

val Context.isDarkTheme: Boolean
    get() = when (FeedPreferences.getInstance(this).overlayTheme.getValue()) {
        "dark", "black"
             -> true

        "light"
             -> false

        else -> resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES // "auto_system"
    }

val Context.isBlackTheme: Boolean
    get() = when (FeedPreferences.getInstance(this).overlayTheme.getValue()) {
        "black", "auto_system_black"
             -> true

        else -> false
    }