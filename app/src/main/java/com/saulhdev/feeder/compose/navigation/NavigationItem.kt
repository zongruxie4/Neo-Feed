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

import com.saulhdev.feeder.R

sealed class NavigationItem(var titleId: Int, var icon: Int, var route: String) {
    object Sources :
        NavigationItem(R.string.title_sources, R.drawable.ic_services_outline_28, "main_sources")

    object Settings :
        NavigationItem(R.string.title_settings, R.drawable.ic_settings_outline_28, "main_settings")

    object Info : NavigationItem(R.string.title_about, R.drawable.ic_info_outline_28, "main_info")
}