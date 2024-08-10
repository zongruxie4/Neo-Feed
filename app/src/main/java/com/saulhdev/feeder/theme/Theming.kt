package com.saulhdev.feeder.theme

import android.content.Context
import android.content.res.Configuration
import android.util.SparseIntArray
import androidx.core.content.ContextCompat
import com.saulhdev.feeder.NFApplication
import com.saulhdev.feeder.R

object Theming {
    val defaultLightThemeColors = createLightTheme()
    val defaultDarkThemeColors = createDarkTheme()

    private fun createLightTheme(): SparseIntArray {
        return SparseIntArray().apply {
            addBasicThings(this)
            put(
                Colors.CARD_BG.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_surface_container
                )
            )
            put(
                Colors.TEXT_COLOR_PRIMARY.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_on_surface
                )
            )
            put(
                Colors.TEXT_COLOR_SECONDARY.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_on_surface_variant
                )
            )
            put(
                Colors.OVERLAY_BG.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_light_background
                )
            )
            put(Colors.IS_LIGHT.ordinal, 1)
        }
    }

    private fun createDarkTheme(): SparseIntArray {
        return SparseIntArray().apply {
            addBasicThings(this)
            put(
                Colors.CARD_BG.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_surface_container
                )
            )
            put(
                Colors.TEXT_COLOR_PRIMARY.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface
                )
            )
            put(
                Colors.TEXT_COLOR_SECONDARY.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_on_surface_variant
                )
            )
            put(
                Colors.OVERLAY_BG.ordinal,
                ContextCompat.getColor(
                    NFApplication.instance,
                    com.google.android.material.R.color.m3_sys_color_dynamic_dark_background
                )
            )
            put(Colors.IS_LIGHT.ordinal, 0)
        }
    }

    private fun addBasicThings(array: SparseIntArray): SparseIntArray {
        return array.apply {
            put(
                Colors.ACCENT_COLOR.ordinal,
                ContextCompat.getColor(NFApplication.instance, R.color.globalAccent)
            )
        }
    }

    fun getThemeBySystem(ctx: Context): SparseIntArray {
        return if (ctx.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) createDarkTheme() else createLightTheme()
    }

    enum class Colors {
        CARD_BG,
        TEXT_COLOR_PRIMARY,
        TEXT_COLOR_SECONDARY,
        ACCENT_COLOR,
        OVERLAY_BG,
        IS_LIGHT
    }
}