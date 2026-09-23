package com.saulhdev.feeder.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.saulhdev.feeder.data.entity.NeoTheme
import com.saulhdev.feeder.manager.localrss.prefs

@RequiresApi(Build.VERSION_CODES.S)
fun dynamicBlackColorScheme(context: Context) =
    dynamicDarkColorScheme(context)
        .copy(
            background = Color.Black,
            surfaceContainerLowest = Color.Black,
        )

@Composable
fun AppTheme(
    themePref: NeoTheme,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val darkTheme =
        when (prefs.appTheme.getValue().nightMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> false
            AppCompatDelegate.MODE_NIGHT_YES -> true
            else -> isSystemInDarkTheme()
        }

    val colorScheme =
        when {
            themePref.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme && themePref.blackOnDark) dynamicBlackColorScheme(context)
                else if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }

            else ->
                rememberDynamicColorScheme(
                    seedColor = Color(themePref.seedColor),
                    isDark = darkTheme,
                    specVersion = ColorSpec.SpecVersion.SPEC_2025,
                    contrastLevel = themePref.contrast,
                    isAmoled = themePref.blackOnDark,
                    style = PaletteStyle.entries[themePref.paletteStyle],
                )
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
