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

package com.saulhdev.feeder.compose.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.saulhdev.feeder.compose.components.webViewerGraph
import com.saulhdev.feeder.compose.pages.AboutPage
import com.saulhdev.feeder.compose.pages.AddFeedPage
import com.saulhdev.feeder.compose.pages.BookmarksPage
import com.saulhdev.feeder.compose.pages.ChangelogScreen
import com.saulhdev.feeder.compose.pages.LicenseScreen
import com.saulhdev.feeder.compose.pages.OverlayPage
import com.saulhdev.feeder.compose.pages.PreferencesPage
import com.saulhdev.feeder.compose.pages.SourcesPage
import com.saulhdev.feeder.compose.pages.articleGraph
import com.saulhdev.feeder.compose.pages.editFeedGraph

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

@Composable
fun NavigationManager(navController: NavHostController) {
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        NavHost(
            navController = navController,
            startDestination = "/",
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 2 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 2 } },
        ) {
            preferenceGraph(route = "/", { PreferencesPage() }) { subRoute ->
                preferenceGraph(route = Routes.SETTINGS, { PreferencesPage() })
                preferenceGraph(route = subRoute(Routes.SOURCES), { SourcesPage() })
                preferenceGraph(route = subRoute(Routes.BOOKMARKS), { BookmarksPage() })
                preferenceGraph(route = subRoute(Routes.OVERLAY), { OverlayPage() })
                preferenceGraph(route = subRoute(Routes.ABOUT), { AboutPage() })
                preferenceGraph(route = Routes.LICENSE, { LicenseScreen() })
                preferenceGraph(route = Routes.CHANGELOG, { ChangelogScreen() })
                preferenceGraph(route = subRoute(Routes.ADD_FEED), { AddFeedPage() })
                editFeedGraph(route = subRoute(Routes.EDIT_FEED))
                webViewerGraph(route = subRoute(Routes.WEB_VIEW))
                articleGraph(route = subRoute(Routes.ARTICLE_VIEW))
            }
        }
    }
}

object Routes {
    const val SETTINGS = "settings"
    const val SOURCES = "sources"
    const val BOOKMARKS = "bookmarks"
    const val OVERLAY = "overlay"
    const val ABOUT = "about"
    const val ADD_FEED = "add_feed"
    const val EDIT_FEED = "edit_feed"
    const val WEB_VIEW = "web_view"
    const val ARTICLE_VIEW = "article_page"
    const val LICENSE = "license"
    const val CHANGELOG = "changelog"
}