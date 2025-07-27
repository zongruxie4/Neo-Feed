/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.ui.theme

import android.util.SparseIntArray
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.extensions.clearLightFlags
import com.saulhdev.feeder.extensions.setLightFlags
import com.saulhdev.feeder.service.OverlayView
import org.koin.java.KoinJavaComponent.inject

/**
 * A class which manages overlay styling.
 * */
class OverlayThemeHolder(private val overlay: OverlayView) {

    val prefs: FeedPreferences by inject(FeedPreferences::class.java)

    /**
     * Current theme colors mapping
     */
    var currentTheme = CardTheme.defaultDarkThemeColors

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