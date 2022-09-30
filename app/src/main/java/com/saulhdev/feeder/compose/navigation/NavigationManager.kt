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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.saulhdev.feeder.InfoScreen
import com.saulhdev.feeder.SettingsScreen
import com.saulhdev.feeder.SourcesScreen
import soup.compose.material.motion.materialSharedAxisX

val LocalNavController = staticCompositionLocalOf<NavController> {
    error("CompositionLocal LocalNavController not present")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavigationManager(navController: NavHostController) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val motionSpec = materialSharedAxisX()
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = NavigationItem.Settings.route,
            enterTransition = { motionSpec.enter.transition(!isRtl, density) },
            exitTransition = { motionSpec.exit.transition(!isRtl, density) },
            popEnterTransition = { motionSpec.enter.transition(isRtl, density) },
            popExitTransition = { motionSpec.exit.transition(isRtl, density) },
        ) {
            preferenceGraph(NavigationItem.Sources.route, { SourcesScreen() })
            preferenceGraph(NavigationItem.Settings.route, { SettingsScreen() })
            preferenceGraph(NavigationItem.Info.route, { InfoScreen() })
        }
    }
}