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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.compose.rememberNavController
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.saulhdev.feeder.compose.components.*
import com.saulhdev.feeder.compose.navigation.NavigationManager
import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = FeedPreferences(this)
        setContent {
            AppTheme {
                MainScreen()
            }
        }

        if (prefs.enabledPlugins.onGetValue().isEmpty()) {
            val list: ArrayList<String> = ArrayList()
            list.add(BuildConfig.APPLICATION_ID)
            prefs.enabledPlugins.onSetValue(list.toSet())
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val title = stringResource(id = R.string.app_name)
    val pageTitle = remember { mutableStateOf(title) }
    Scaffold(
        topBar = { TopBar(pageTitle) },
        bottomBar = { BottomNavigationBar(navController) },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                NavigationManager(navController = navController)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background
    )
}

@Composable
fun TopBar(pageTitle: MutableState<String>) {
    TopAppBar(
        title = {
            Text(
                text = pageTitle.value,
                fontSize = 18.sp,
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background,
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        var rssURL by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val prefs = FeedPreferences(context)
        val feedList = prefs.feedList.onGetValue().map { SavedFeedModel(JSONObject(it)) }
        val rssList = remember { mutableStateOf(feedList) }
        val showDialog = remember { mutableStateOf(false) }
        OutlinedTextField(
            value = rssURL,
            onValueChange = { rssURL = it },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = false,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F),
                textColor = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            shape = MaterialTheme.shapes.large,
            label = { androidx.compose.material.Text(text = stringResource(id = R.string.add_input_hint)) }
        )

        Spacer(modifier = Modifier.height(8.dp))
        DialogPositiveButton(
            textId = R.string.manager_add,
            onClick = {
                coroutineScope.launch {
                    var data: Channel? = null
                    withContext(Dispatchers.Default) {
                        val parser = Parser.Builder()
                            .okHttpClient(OkHttpClient())
                            .build()
                        try {
                            data = parser.getChannel(rssURL)
                        } catch (_: Exception) {

                        }
                    }
                    data ?: run {
                        Toast.makeText(context, "URL is not a RSS feed!", Toast.LENGTH_LONG)
                            .show()
                        return@launch
                    }
                    val title = data!!.title ?: "Unknown"
                    val savedFeedModel = SavedFeedModel(
                        title,
                        data!!.description ?: "",
                        rssURL,
                        data!!.image?.url ?: ""
                    )
                    rssList.value = rssList.value + savedFeedModel
                    rssURL = ""
                    val stringSet = rssList.value.map {
                        it.asJson().toString()
                    }.toSet()
                    prefs.feedList.onSetValue(stringSet)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(rssList.value) { item ->
                FeedItem(
                    feedTitle = item.name,
                    feedURL = item.feedUrl,
                    description = item.description,
                    onRemoveAction = {
                        showDialog.value = true
                    }
                )

                if (showDialog.value) {
                    CustomDialog(
                        item = item,
                        setShowDialog = {
                            showDialog.value = it
                        }
                    ) {
                        rssList.value = rssList.value - item
                        val stringSet = rssList.value.map {
                            it.asJson().toString()
                        }.toSet()
                        prefs.feedList.onSetValue(stringSet)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDialog(
    item: SavedFeedModel,
    setShowDialog: (Boolean) -> Unit,
    onPositiveClick: () -> Unit
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                        text = stringResource(id = R.string.remove_desc, item.name),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Default
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DialogNegativeButton(
                            textId = R.string.remove_action_nope,
                        ) {
                            setShowDialog(false)
                        }
                        Spacer(Modifier.weight(1f))
                        DialogPositiveButton(
                            textId = R.string.remove_action_yes,
                            onClick = {
                                onPositiveClick()
                                setShowDialog(false)
                            }
                        )
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
                stringResource(id = R.string.pref_cat_overlay),
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
        prefs.developer,
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