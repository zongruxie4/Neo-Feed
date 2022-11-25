package com.saulhdev.feeder.launcherapi

import android.annotation.TargetApi
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.util.SparseIntArray
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R
import com.saulhdev.feeder.overlay.OverlayView
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.Theming
import com.saulhdev.feeder.utils.clearLightFlags
import com.saulhdev.feeder.utils.isLight
import com.saulhdev.feeder.utils.toInt

/**
 * A class which manages overlay styling.
 * */
class OverlayThemeHolder(private val context: Context, private val overlay: OverlayView) {

    val prefs = FeedPreferences(context)

    /**
     * Current theme colors mapping
     */
    var currentTheme = Theming.defaultDarkThemeColors

    /**
     * Card background
     */
    var cardBgPref = prefs.cardBackground.onGetValue()

    /**
     * Overlay background
     */
    var overlayBgPref = prefs.overlayBackground.onGetValue()

    /**
     * If we should apply light statusbar/navbar
     */
    var shouldUseSN = false

    /**
     * If we are applied light statusbar/navbar
     */
    var isSNApplied = false

    /**
     * If we are using system colors instead of [LauncherAPI]
     */
    var systemColors = false

    /**
     * Parses [cardBgPref] into color integer
     */
    private val cardBackground: Int get() = when (cardBgPref) {
        "light" -> ContextCompat.getColor(context, R.color.card_bg)
        "dark" -> ContextCompat.getColor(context, R.color.card_bg_dark)
        else -> Color.BLACK
    }

    /**
     * Parses [overlayBgPref] into color integer
     */
    private val overlayBackground: Int get() = when (overlayBgPref) {
        "white" -> Color.WHITE
        "dark" -> ContextCompat.getColor(context, R.color.bg_dark)
        "launcher_primary" -> if (prefs.systemColors.onGetValue()) primaryWallColor() else overlay.apiInstance.backgroundColorHint
        "launcher_secondary" -> if (prefs.systemColors.onGetValue()) secondaryWallColor() else overlay.apiInstance.backgroundColorHintSecondary
        "launcher_tertiary" -> if (prefs.systemColors.onGetValue()) tertiaryWallColor() else overlay.apiInstance.backgroundColorHintTertiary
        else -> Color.BLACK
    }

    /**
     * Replaces the color mapping ([currentTheme]) with config-specified values
     */
    fun setTheme(theme: SparseIntArray) {
        currentTheme = theme

        shouldUseSN = if (overlayBgPref != "theme") {
            currentTheme.put(Theming.Colors.OVERLAY_BG.ordinal, overlayBackground)
            overlayBackground.isLight()
        } else {
            currentTheme.get(Theming.Colors.IS_LIGHT.ordinal) == 1
        }

        if (!shouldUseSN && isSNApplied) {
            isSNApplied = false
            overlay.window.decorView.clearLightFlags()
        }

        if (cardBgPref != "theme") {
            currentTheme.put(Theming.Colors.CARD_BG.ordinal, cardBackground)
        }

        Log.d("OTH", currentTheme.toString())
    }

    companion object {
        @ColorInt
        @TargetApi(Build.VERSION_CODES.O_MR1)
        fun primaryWallColor(): Int {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return Color.BLACK
            val wc = WallpaperManager.getInstance(NFApplication.instance)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            wc ?: Color.BLACK
            return wc!!.primaryColor.toInt()
        }

        @ColorInt
        @TargetApi(Build.VERSION_CODES.O_MR1)
        fun secondaryWallColor(): Int {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return Color.BLACK
            val wc = WallpaperManager.getInstance(NFApplication.instance)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            wc ?: Color.BLACK
            return wc!!.secondaryColor.toInt()
        }

        @ColorInt
        @TargetApi(Build.VERSION_CODES.O_MR1)
        fun tertiaryWallColor(): Int {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return Color.BLACK
            val wc = WallpaperManager.getInstance(NFApplication.instance)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            wc ?: Color.BLACK
            wc?.tertiaryColor ?: Color.BLACK
            return wc!!.tertiaryColor.toInt()
        }
    }
}