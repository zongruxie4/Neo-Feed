/*
 * This file is part of Neo Feed
 * Copyright (c) 2022   Saul Henriquez <henriquez.saul@gmail.com>
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

package com.saulhdev.feeder.utils

import android.content.Context
import androidx.compose.ui.Modifier
import com.saulhdev.feeder.R

inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

fun getThemes(context: Context): Map<String, String> {
    return mapOf(
        "auto_launcher" to context.resources.getString(R.string.theme_auto_launcher),
        "auto_system" to context.resources.getString(R.string.theme_auto_system),
        "light" to context.resources.getString(R.string.theme_light),
        "dark" to context.resources.getString(R.string.theme_dark)
    )
}

fun getTransparencyOptions(context: Context): Map<String, String> {
    return mapOf(
        "non_transparent" to context.resources.getString(R.string.transparency_non_transparent),
        "more_half" to context.resources.getString(R.string.transparency_low_transparency),
        "half" to context.resources.getString(R.string.transparency_mid_transparency),
        "less_half" to context.resources.getString(R.string.transparency_high_transparency),
        "transparent" to context.resources.getString(R.string.transparency_transparent)
    )
}