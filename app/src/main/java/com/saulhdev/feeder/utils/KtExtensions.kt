package com.saulhdev.feeder.utils

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

const val LIGHT_BORDER = 0.5f

fun Int.isLight() = ColorUtils.calculateLuminance(this) > LIGHT_BORDER
fun Int.isDark() = ColorUtils.calculateLuminance(this) < LIGHT_BORDER

fun Bundle.dump(tag: String) {
    keySet().forEach {
        val item = get(it)
        item ?: return@forEach
        Log.d(tag, "[$it] $item")
    }
}

fun Window.setLightFlags() {
    val controller = WindowCompat.getInsetsController(this, this.decorView)
    controller.isAppearanceLightStatusBars = true
    controller.isAppearanceLightNavigationBars = true
}

fun Window.clearLightFlags() {
    val controller = WindowCompat.getInsetsController(this, this.decorView)
    controller.isAppearanceLightStatusBars = false
    controller.isAppearanceLightNavigationBars = false
}

@ColorInt
fun Color?.toInt(): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return Color.BLACK
    this ?: Color.BLACK
    return Color.rgb(this!!.red(), this.green(), this.blue())
}