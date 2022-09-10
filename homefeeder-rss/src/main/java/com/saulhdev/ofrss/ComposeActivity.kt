package com.saulhdev.ofrss

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saulhdev.ofrss.compose.DefaultComposeView
import com.saulhdev.ofrss.theme.AppTheme

class ComposeActivity : AppCompatActivity(){
    lateinit var navController: NavHostController

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                navController = rememberAnimatedNavController()
                DefaultComposeView(navController)
            }
        }
    }

    companion object {
        fun createIntent(context: Context, destination: String): Intent {
            val uri = "android-app://androidx.navigation//$destination".toUri()
            return Intent(Intent.ACTION_VIEW, uri, context, ComposeActivity::class.java)
        }
    }
}