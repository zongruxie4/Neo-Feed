package com.saulhdev.feeder.compose.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.icon.Phosphor
import com.saulhdev.feeder.compose.icon.phosphor.Copyleft
import com.saulhdev.feeder.compose.icon.phosphor.ListDashes

open class PageItem(
    @StringRes val titleId: Int,
    val icon: ImageVector,
    val route: String,
) {
    companion object {
        val AboutLicense = PageItem(
            titleId = R.string.about_licenses,
            icon = Phosphor.Copyleft,
            route = Routes.LICENSE,
        )
        val AboutChangelog = PageItem(
            titleId = R.string.about_changelog,
            icon = Phosphor.ListDashes,
            route = Routes.CHANGELOG,
        )
    }
}