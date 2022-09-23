/*
 * This file is part of Omega Feeder
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

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.saulhdev.feeder.InfoScreen
import com.saulhdev.feeder.SettingsScreen
import com.saulhdev.feeder.SourcesScreen
import com.saulhdev.feeder.compose.pages.WebViewPage

@Composable
fun NavigationManager(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationItem.Settings.route) {
        composable(NavigationItem.Sources.route) {
            SourcesScreen()
        }
        composable(NavigationItem.Settings.route) {
            SettingsScreen()
        }
        composable(NavigationItem.Info.route) {
            InfoScreen()
        }
        composable("web_view_page/{url}") {
            val url = it.arguments?.getString("url")
            if (url != null) {
                WebViewPage(url = url)
            }
        }
    }
}