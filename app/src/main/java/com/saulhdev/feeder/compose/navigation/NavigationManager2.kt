package com.saulhdev.feeder.compose.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.saulhdev.feeder.compose.components.webViewerGraph
import com.saulhdev.feeder.compose.pages.articleGraph

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationManager2(navController: NavHostController) {
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "/",
            enterTransition = { fadeIn()  + slideInHorizontally { it -> it } },
            exitTransition = { fadeOut() + slideOutHorizontally { it -> -it/2 } },
            popEnterTransition = { fadeIn()  + slideInHorizontally { it -> -it } },
            popExitTransition = { fadeOut()  + slideOutHorizontally { it -> it/2 } },
        ) {
            preferenceGraph(route = "/", { }) { subRoute ->
                webViewerGraph(route = subRoute(Routes.WEB_VIEW))
                articleGraph(route = subRoute(Routes.ARTICLE_VIEW))
            }
        }
    }
}