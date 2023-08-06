package com.saulhdev.feeder.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlin.system.exitProcess

class Utilities {
    fun restartFeed(context: Context) {
        val pm: PackageManager = context.packageManager
        var intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val componentName: ComponentName = intent.resolveActivity(pm)
        if (!context.packageName.equals(componentName.packageName)) {
            intent = pm.getLaunchIntentForPackage(context.packageName)!!
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        restartFeed(context, intent)
    }

    private fun restartFeed(context: Context, intent: Intent?) {
        context.startActivity(intent)

        // Create a pending intent so the application is restarted after System.exit(0) was called.
        // We use an AlarmManager to call this intent in 100ms
        val mPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)

        // Kill the application
        exitProcess(0)
    }
}

fun Context.shareIntent(url: String, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, title)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)

    startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
}