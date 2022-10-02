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

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.saulhdev.feeder.compose.components.BottomNavigationBar
import com.saulhdev.feeder.compose.components.FeedItem
import com.saulhdev.feeder.compose.components.PreferenceGroup
import com.saulhdev.feeder.compose.components.StringSelectionPrefDialogUI
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.compose.navigation.NavigationItem
import com.saulhdev.feeder.compose.navigation.NavigationManager
import com.saulhdev.feeder.compose.navigation.subRoute
import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    lateinit var prefs: FeedPreferences
    private val prefsToWatch = arrayOf(
        "pref_overlay_theme",
        "pref_overlay_transparency",
        "pref_overlay_compact",
        "pref_overlay_system_colors",
        "pref_overlay_background",
        "pref_overlay_card_background"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen()
            }
        }
        prefs = FeedPreferences(this)
        if (prefs.enabledPlugins.onGetValue().isEmpty()) {
            val list: ArrayList<String> = ArrayList()
            list.add(BuildConfig.APPLICATION_ID)
            prefs.enabledPlugins.onSetValue(list.toSet())
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        prefs.sharedPrefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        prefs.sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (prefsToWatch.contains(key)) {
            recreate()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val navController = rememberAnimatedNavController()
    val title = stringResource(id = R.string.app_name)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val scope = CoroutineScope(Dispatchers.Main)
    val destination = subRoute(name = "add_feed")
    ViewWithActionBar(
        title = title,
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            if (navBackStackEntry?.destination?.route == NavigationItem.Sources.route) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            context.startActivity(
                                ComposeActivity.createIntent(
                                    context,
                                    destination
                                )
                            )
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    backgroundColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

        },
        showBackButton = false,
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavigationManager(navController = navController)
        }
    }
}

@Composable
fun SourcesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        val context = LocalContext.current
        val prefs = FeedPreferences(context)
        val feedList = prefs.feedList.onGetValue().map { SavedFeedModel(JSONObject(it)) }
        val rssList = remember { mutableStateOf(feedList) }
        val showDialog = remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(rssList.value) { item ->
                FeedItem(
                    feedTitle = item.title,
                    feedURL = item.url,
                    description = item.description,
                    onRemoveAction = {
                        showDialog.value = true
                    }
                )
                if (showDialog.value) {
                    Dialog(
                        onDismissRequest = { showDialog.value = false },
                        DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.remove_title),
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontFamily = FontFamily.Default,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(
                                            id = R.string.remove_desc,
                                            item.title
                                        ),
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily.Default
                                        )
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        TextButton(
                                            shape = RoundedCornerShape(16.dp),
                                            onClick = {
                                                showDialog.value = false
                                            }
                                        ) {
                                            Text(
                                                text = stringResource(id = android.R.string.cancel),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(
                                                    vertical = 5.dp,
                                                    horizontal = 8.dp
                                                )
                                            )
                                        }

                                        Spacer(Modifier.weight(1f))

                                        TextButton(
                                            shape = RoundedCornerShape(16.dp),
                                            onClick = {
                                                rssList.value = rssList.value - item
                                                val stringSet = rssList.value.map {
                                                    it.asJson().toString()
                                                }.toSet()
                                                prefs.feedList.onSetValue(stringSet)
                                                showDialog.value = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary.copy(
                                                    0.65f
                                                ),
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text(
                                                text = stringResource(id = android.R.string.ok),
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(
                                                    top = 5.dp,
                                                    bottom = 5.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val prefs = FeedPreferences(LocalContext.current)
    val themePrefs = listOf(
        prefs.overlayTheme,
        prefs.overlayTransparency,
        prefs.overlayCompact,
        prefs.systemColors,
        prefs.overlayBackground,
        prefs.cardBackground
    )

    val debugPrefs = listOf(
        prefs.debugging,
        prefs.contentDebugging
    )

    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onPrefDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PreferenceGroup(
                stringResource(id = R.string.pref_cat_overlay),
                prefs = themePrefs,
                onPrefDialog = onPrefDialog
            )
            PreferenceGroup(
                stringResource(id = R.string.pref_cat_debug),
                prefs = debugPrefs,
                onPrefDialog = onPrefDialog
            )
        }
    }

    if (openDialog.value) {
        when (dialogPref) {
            is FeedPreferences.StringSelectionPref -> StringSelectionPrefDialogUI(
                pref = dialogPref as FeedPreferences.StringSelectionPref,
                openDialogCustom = openDialog
            )
        }
    }
}

@Composable
fun InfoScreen() {
    val prefs = FeedPreferences(LocalContext.current)
    val aboutInfo = listOf(
        prefs.developer1,
        prefs.developer2,
        prefs.telegramChannel,
        prefs.sourceCode
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(2f))
            ) {
                ResourcesCompat.getDrawable(
                    LocalContext.current.resources,
                    R.mipmap.ic_launcher,
                    LocalContext.current.theme
                )?.let { drawable ->
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier
                            .requiredSize(84.dp)
                            .padding(top = 16.dp)
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.app_name),
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(id = R.string.app_version) + ": "
                        + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(id = R.string.app_id) + ": " + BuildConfig.APPLICATION_ID,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(
                    stringResource(id = R.string.pref_cat_contact),
                    prefs = aboutInfo
                )
            }
        }
    }
}