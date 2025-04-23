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

package com.saulhdev.feeder.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.DynamicColors
import com.saulhdev.feeder.utils.extensions.isBlackTheme

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dynamicColors = DynamicColors.isDynamicColorAvailable()
    val blackTheme = context.isBlackTheme
    MaterialTheme(
        colorScheme = when {
            darkTheme && dynamicColors && blackTheme -> dynamicDarkColorScheme(context).copy(
                background = Color.Black,
            )

            darkTheme && dynamicColors               -> dynamicDarkColorScheme(context)
            dynamicColors                            -> dynamicLightColorScheme(context)
            darkTheme && blackTheme                  -> BlackColors
            darkTheme                                -> DarkColors
            else                                     -> LightColors // Light Theme
        },
        content = content
    )
}

private val LightColors = lightColorScheme(
    background = LightBackground,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val DarkColors = lightColorScheme(
    background = DarkBackground,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)
private val BlackColors = DarkColors.copy(
    background = Color.Black,
)