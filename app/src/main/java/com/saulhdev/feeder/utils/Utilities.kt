package com.saulhdev.feeder.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess


class Utilities {
    fun restartApp(context: Context) {
        val pm = context.packageManager

        var intent: Intent? = Intent(Intent.ACTION_MAIN)
        intent!!.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val componentName = intent.resolveActivity(pm)
        if (context.packageName != componentName.packageName) {
            intent = pm.getLaunchIntentForPackage(context.packageName)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        restartApp(context, intent)
    }

    fun restartApp(context: Context, intent: Intent?) {
        context.startActivity(intent)

        // Create a pending intent so the application is restarted after System.exit(0) was called.
        // We use an AlarmManager to call this intent in 100ms
        val mPendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)


        exitProcess(0)
    }
}