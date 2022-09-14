package com.saulhdev.feeder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.compose.rememberNavController
import com.saulhdev.feeder.compose.components.BottomNavigationBar
import com.saulhdev.feeder.compose.components.PreferenceGroup
import com.saulhdev.feeder.compose.components.StringSelectionPrefDialogUI
import com.saulhdev.feeder.compose.navigation.NavigationManager
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }

/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_plugins, R.id.navigation_settings, R.id.navigation_about
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showStorageAlert()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_settings)
            } else {
                showStorageAlert()
            }
        }
    }

    private fun showStorageAlert() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.storage_alert)
            .setMessage(R.string.storage_desc)
            .setPositiveButton(R.string.storage_action) { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setCancelable(false)
            .show()
    }*/
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopBar() },
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
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 18.sp,
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background,
    )
}

@Composable
fun SourcesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Sources",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Composable
fun SettingsScreen() {
    val prefs = FeedPreferences(LocalContext.current)
    val themePrefs = listOf(
        prefs.overlayTheme
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

@Preview
@Composable
fun InfoScreenPreview() {
    InfoScreen()
}