package com.saulhdev.feeder.compose.navigation

import com.saulhdev.feeder.R

sealed class NavigationItem(var titleId: Int, var icon: Int, var route: String) {
    object Sources :
        NavigationItem(R.string.title_sources, R.drawable.ic_services_outline_28, "main_sources")

    object Settings :
        NavigationItem(R.string.title_settings, R.drawable.ic_settings_outline_28, "main_settings")

    object Info : NavigationItem(R.string.title_about, R.drawable.ic_info_outline_28, "main_info")
}