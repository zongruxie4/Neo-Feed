/*
 * This file is part of Omega Feeder
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

import androidx.compose.ui.Modifier

inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier
): Modifier =
    if (condition) factory() else this

fun getThemes(): Map<String, String> {
    return mapOf(
        "auto_launcher" to "Automatically (from launcher)",
        "auto_system" to "Automatically (from system)",
        "light" to "Light",
        "dark" to "Dark"
    )
}

fun getTransparencyOptions(): Map<String, String> {
    return mapOf(
        "non_transparent" to "Non-transparent",
        "more_half" to "75% transparent",
        "half" to "50% transparent",
        "less_half" to "25% transparent",
        "transparent" to "Transparent"
    )
}