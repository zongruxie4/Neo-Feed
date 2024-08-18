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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ComposeWebView
import com.saulhdev.feeder.compose.pages.AboutPage
import com.saulhdev.feeder.compose.pages.AddFeedPage
import com.saulhdev.feeder.compose.pages.ArticleScreen
import com.saulhdev.feeder.compose.pages.ChangelogScreen
import com.saulhdev.feeder.compose.pages.DiscoverPage
import com.saulhdev.feeder.compose.pages.EditFeedPage
import com.saulhdev.feeder.compose.pages.LicenseScreen
import com.saulhdev.feeder.compose.pages.MainPage
import com.saulhdev.feeder.compose.pages.OverlayPage
import com.saulhdev.feeder.compose.pages.PreferencesPage
import com.saulhdev.feeder.compose.pages.SourcesPage
import com.saulhdev.feeder.icon.Phosphor
import com.saulhdev.feeder.icon.phosphor.GearSix
import com.saulhdev.feeder.icon.phosphor.Graph
import com.saulhdev.feeder.icon.phosphor.Info
import kotlinx.serialization.Serializable

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

const val NAV_BASE = "nf-navigation://androidx.navigation/"

@Composable
fun NavigationManager(navController: NavHostController) {
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        NavHost(
            navController = navController,
            startDestination = NavRoute.Main(),
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 2 } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it / 2 } },
        ) {
            composable<NavRoute.Main>(
                deepLinks = listOf(navDeepLink { uriPattern = "$NAV_BASE${Routes.MAIN}/{page}" })
            ) {
                val args = it.toRoute<NavRoute.Main>()
                MainPage(args.page)
            }
            composable<NavRoute.About> { AboutPage() }
            composable<NavRoute.License> { LicenseScreen() }
            composable<NavRoute.Changelog> { ChangelogScreen() }
            composable<NavRoute.AddFeed> { AddFeedPage() }
            composable<NavRoute.Explore> { DiscoverPage() }
            composable<NavRoute.EditFeed>(
                deepLinks = listOf(navDeepLink {
                    uriPattern = "$NAV_BASE${Routes.EDIT_FEED}/{feedId}"
                })
            ) {
                val args = it.toRoute<NavRoute.EditFeed>()
                EditFeedPage(args.feedId)
            }
            composable<NavRoute.WebView>(
                deepLinks = listOf(navDeepLink { uriPattern = "$NAV_BASE${Routes.WEB_VIEW}/{url}" })
            ) {
                val args = it.toRoute<NavRoute.WebView>()
                ComposeWebView(args.url)
            }
            composable<NavRoute.ArticleView>(
                deepLinks = listOf(navDeepLink {
                    uriPattern = "$NAV_BASE${Routes.ARTICLE_VIEW}/{id}"
                })
            ) {
                val args = it.toRoute<NavRoute.ArticleView>()
                ArticleScreen(args.id)
            }
        }
    }
}

object Routes {
    const val MAIN = "main"
    const val EDIT_FEED = "edit_feed"
    const val WEB_VIEW = "web_view"
    const val ARTICLE_VIEW = "article_page"
    const val EXPLORE = "explore"
}

sealed class NavItem(
    val title: Int,
    val icon: ImageVector,
    val content: @Composable () -> Unit = {}
) {
    data object Overlay :
        NavItem(R.string.app_name, Phosphor.Info, {
            OverlayPage()
        })

    data object Settings :
        NavItem(R.string.title_settings, Phosphor.GearSix, {
            PreferencesPage()
        })

    data object Sources :
        NavItem(R.string.title_sources, Phosphor.Graph, {
            SourcesPage()
        })
}

@Serializable
open class NavRoute {
    @Serializable
    data object About : NavRoute()

    @Serializable
    data object AddFeed : NavRoute()

    @Serializable
    data class ArticleView(val id: Long = 0L) : NavRoute()

    @Serializable
    data object Changelog : NavRoute()

    @Serializable
    data class EditFeed(val feedId: Long = 0L) : NavRoute()

    @Serializable
    data class Main(val page: Int = 0) : NavRoute()

    @Serializable
    data object License : NavRoute()

    @Serializable
    data object Explore : NavRoute()

    @Serializable
    data class WebView(val url: String = "") : NavRoute()
}