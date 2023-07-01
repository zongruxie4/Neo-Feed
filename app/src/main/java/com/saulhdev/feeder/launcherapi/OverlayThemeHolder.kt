package com.saulhdev.feeder.launcherapi

import android.content.Context
import android.util.SparseIntArray
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.R
import com.saulhdev.feeder.overlay.OverlayView
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.Theming
import com.saulhdev.feeder.utils.clearLightFlags

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
        "theme" -> currentTheme.get(Theming.Colors.CARD_BG.ordinal)
        "dark" -> ContextCompat.getColor(context, R.color.card_bg_dark)
        else -> ContextCompat.getColor(context, R.color.card_bg)
    }

    /**
     * Replaces the color mapping ([currentTheme]) with config-specified values
     */
    fun setTheme(theme: SparseIntArray) {
        currentTheme = theme

        if (!shouldUseSN && isSNApplied) {
            isSNApplied = false
            overlay.window.decorView.clearLightFlags()
        }

        if (cardBgPref != "theme") {
            currentTheme.put(Theming.Colors.CARD_BG.ordinal, cardBackground)
        }
    }
}