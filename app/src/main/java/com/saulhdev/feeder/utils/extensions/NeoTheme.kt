package com.saulhdev.feeder.utils.extensions

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.materialkolor.Contrast
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.entity.NeoTheme
import com.saulhdev.feeder.ui.theme.presetColors

fun Context.themeSummary(theme: NeoTheme): String {
    val nightModeStr = when (theme.nightMode) {
        AppCompatDelegate.MODE_NIGHT_NO
             -> getString(R.string.light)

        AppCompatDelegate.MODE_NIGHT_YES
             -> getString(R.string.dark)

        else -> getString(R.string.system)
    }

    if (theme.dynamicColor) return "${getString(R.string.dynamic_theme)} $nightModeStr"

    val colorStr = getString(
        presetColors[theme.seedColor]
            ?: R.string.color_purple
    )

    val base = if (theme.blackOnDark && isDarkTheme) {
        "${getString(R.string.theme_black)} $colorStr"
    } else {
        "$nightModeStr $colorStr"
    }

    return if (theme.contrast != Contrast.Default.value) {
        val contrastStr = when (theme.contrast) {
            Contrast.Medium.value -> getString(R.string.contrast_medium)
            Contrast.High.value   -> getString(R.string.contrast_high)
            else                  -> getString(R.string.contrast_default)
        }
        "$base ($contrastStr)"
    } else {
        base
    }
}