package com.saulhdev.feeder.compose.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.saulhdev.feeder.compose.components.webViewerGraph
import soup.compose.material.motion.materialSharedAxisX

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationManager2(navController: NavHostController) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val motionSpec = materialSharedAxisX()
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = "/",
            enterTransition = { motionSpec.enter.transition(!isRtl, density) },
            exitTransition = { motionSpec.exit.transition(!isRtl, density) },
            popEnterTransition = { motionSpec.enter.transition(isRtl, density) },
            popExitTransition = { motionSpec.exit.transition(isRtl, density) },
        ) {
            preferenceGraph(route = "/", { }) { subRoute ->
                webViewerGraph(route = subRoute(Routes.WEB_VIEW))
            }
        }
    }
}