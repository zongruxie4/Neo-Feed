package com.saulhdev.feeder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.navigation.NavigationManager
import com.saulhdev.feeder.ui.theme.AppTheme
import org.koin.java.KoinJavaComponent.inject

class MainActivity : ComponentActivity(){
    private lateinit var navController: NavHostController
    private val prefs: FeedPreferences by inject(FeedPreferences::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            navController = rememberNavController()
            TransparentSystemBars()
            AppTheme (
                dynamicColor = prefs.dynamicColor.getValue(),
            ){
                NavigationManager(navController = navController)
            }
        }
    }

    @Composable
    fun TransparentSystemBars() {
        //TODO: get key 2 and key 1 from preferences
        val isDarkTheme = false // Replace with actual preference retrieval logic
        DisposableEffect(isDarkTheme, false) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
                navigationBarStyle = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { isDarkTheme },
            )
            onDispose {}
        }
    }
}