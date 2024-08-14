package com.saulhdev.feeder.compose.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.saulhdev.feeder.R
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.Copyleft
import com.saulhdev.feeder.icon.phosphor.ListDashes

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector,
    val route: NavRoute,
) {
    companion object {
        val AboutLicense = PageItem(
            titleId = R.string.about_licenses,
            icon = Phosphor.Copyleft,
            route = NavRoute.License,
        )
        val AboutChangelog = PageItem(
            titleId = R.string.about_changelog,
            icon = Phosphor.ListDashes,
            route = NavRoute.Changelog,
        )
    }
}