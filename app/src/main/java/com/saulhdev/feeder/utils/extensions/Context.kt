package com.saulhdev.feeder.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import com.google.android.material.color.DynamicColors
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences

interface ToastMaker {
    suspend fun makeToast(text: String)
    suspend fun makeToast(@StringRes resId: Int)
}

fun Context.makeToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.launchView(url: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        )
    )
}

fun Context.shareIntent(url: String, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TITLE, title)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, title)
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)

    startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
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