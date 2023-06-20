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

package com.saulhdev.feeder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.saulhdev.feeder.compose.navigation.NavigationManager2
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.AppTheme

class ComposeActivity : AppCompatActivity() {
    lateinit var prefs: FeedPreferences
    lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = FeedPreferences(this)
        if (supportActionBar != null) {
            supportActionBar?.hide()
        }
        setContent {
            AppTheme(
                darkTheme = when (prefs.overlayTheme.onGetValue()) {
                    "auto_system" -> isSystemInDarkTheme()
                    "dark" -> true
                    else -> false
                }
            ) {
                navController = rememberNavController()
                NavigationManager2(navController = navController)
            }
        }
    }

    companion object {
        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            Log.d("ComposeActivity", "uri: $uri")
            return Intent(Intent.ACTION_VIEW, uri, context, ComposeActivity::class.java)
        }
    }
}