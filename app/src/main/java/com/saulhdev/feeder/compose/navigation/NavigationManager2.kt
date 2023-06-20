package com.saulhdev.feeder.compose.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.saulhdev.feeder.compose.components.webViewerGraph
import com.saulhdev.feeder.compose.pages.articleGraph

@Composable
fun NavigationManager2(navController: NavHostController) {
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
            preferenceGraph(route = "/", { }) { subRoute ->
                webViewerGraph(route = subRoute(Routes.WEB_VIEW))
                articleGraph(route = subRoute(Routes.ARTICLE_VIEW))
            }
        }
    }
}