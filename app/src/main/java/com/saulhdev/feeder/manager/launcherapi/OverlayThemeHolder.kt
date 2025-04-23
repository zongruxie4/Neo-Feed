package com.saulhdev.feeder.manager.launcherapi

import android.util.SparseIntArray
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.ui.compose.theme.Theming
import com.saulhdev.feeder.ui.overlay.OverlayView
import com.saulhdev.feeder.utils.extensions.clearLightFlags
import com.saulhdev.feeder.utils.extensions.setLightFlags
import org.koin.java.KoinJavaComponent.inject

/**
 * A class which manages overlay styling.
 * */
class OverlayThemeHolder(private val overlay: OverlayView) {

    val prefs: FeedPreferences by inject(FeedPreferences::class.java)

    /**
     * Current theme colors mapping
     */
    var currentTheme = Theming.defaultDarkThemeColors

    /**
     * If we should apply light statusbar/navbar
     */
    var shouldUseSN = true

    /**
     * If we are applied light statusbar/navbar
     */
    var isSNApplied = false

    /**
     * If we are using system colors instead of [LauncherAPI]
     */
    var systemColors = false

    /**
     * Replaces the color mapping ([currentTheme]) with config-specified values
     */
    fun setTheme(theme: SparseIntArray) {
        currentTheme = theme

        if (shouldUseSN && !isSNApplied) {
            isSNApplied = true
            overlay.window.setLightFlags()
        } else if (shouldUseSN && isSNApplied) {
            isSNApplied = false
            overlay.window.clearLightFlags()
        }
    }
}