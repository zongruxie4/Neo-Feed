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

package com.saulhdev.feeder.compose.pages

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import coil.annotation.ExperimentalCoilApi
import com.saulhdev.feeder.BuildConfig
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ContributorRow
import com.saulhdev.feeder.compose.components.ItemLink
import com.saulhdev.feeder.compose.components.PreferenceGroup
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.utils.urlDecode
import java.io.InputStream

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AboutPage() {
    val title = stringResource(id = R.string.title_about)
    ViewWithActionBar(
        title = title,
    ) { paddingValues ->
        val prefs = FeedPreferences(LocalContext.current)

        Column(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(), start = 8.dp, end = 8.dp
                )
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = stringResource(id = R.string.app_version) + ": "
                            + BuildConfig.VERSION_NAME + " ( Build " + BuildConfig.VERSION_CODE + " )",
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(id = R.string.app_id) + ": " + BuildConfig.APPLICATION_ID,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                links.map { link ->
                    ItemLink(
                        iconResId = link.iconResId,
                        label = stringResource(id = link.labelResId),
                        modifier = Modifier.weight(1f),
                        url = link.url
                    )
                }
            }

            PreferenceGroup(heading = stringResource(id = R.string.about_team)) {
                val size = contributors.size
                contributors.forEachIndexed { i, it ->
                    ContributorRow(
                        nameId = it.name,
                        roleId = it.descriptionRes,
                        photoUrl = it.photoUrl,
                        url = it.webpage,
                        index = i,
                        groupSize = size
                    )
                    if (i + 1 < size) Spacer(modifier = Modifier.height(2.dp))
                }
            }

            PreferenceGroup(
                heading = stringResource(id = R.string.about_licenses),
                prefs = listOf(prefs.aboutLicense, prefs.aboutChangelog)
            )
        }
    }
}


private data class Link(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val url: String
)

private data class TeamMember(
    @StringRes val name: Int,
    @StringRes val descriptionRes: Int,
    val photoUrl: String,
    val webpage: String
)

private val links = listOf(
    Link(
        iconResId = R.drawable.ic_github,
        labelResId = R.string.about_source_code,
        url = "https://github.com/NeoApplications/Neo-Launcher"
    ),
    Link(
        iconResId = R.drawable.ic_channel,
        labelResId = R.string.about_channel,
        url = "https://t.me/neo_applications"
    ),
    Link(
        iconResId = R.drawable.ic_community,
        labelResId = R.string.about_community,
        url = "https://t.me/neo_launcher"
    )
)

private val contributors = listOf(
    TeamMember(
        name = R.string.about_developer,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/6044050",
        webpage = "https://github.com/saulhdev"
    ),
    TeamMember(
        name = R.string.about_developer2,
        descriptionRes = R.string.author_role,
        photoUrl = "https://avatars.githubusercontent.com/u/40302595",
        webpage = "https://github.com/machiav3lli"
    )
)

@Composable
fun LicenseScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_open_source),
    ) {
        PreferencesWebView(url = "file:///android_asset/license.htm")
    }
}

@Composable
fun ChangelogScreen() {
    ViewWithActionBar(
        title = stringResource(R.string.about_changelog),
    ) {
        PreferencesWebView(url = "file:///android_asset/changelog.htm")
        Spacer(modifier = Modifier.requiredHeight(50.dp))
    }
}

@Composable
fun PreferencesWebView(url: String) {

    val cssFile = "light.css"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        if (url.startsWith("file:///android_asset")) {
                            try {
                                settings.javaScriptEnabled = true
                                val inputStream: InputStream = context.assets.open(cssFile)
                                val buffer = ByteArray(inputStream.available())
                                inputStream.read(buffer)
                                inputStream.close()
                                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                                loadUrl(
                                    "javascript:(function() { " +
                                            "var head  = document.getElementsByTagName('head')[0];" +
                                            "var style = document.createElement('style');" +
                                            "style.type = 'text/css';" +
                                            "style.innerHTML =  window.atob('" + encoded + "');" +
                                            "head.appendChild(style);" +
                                            "})()"
                                )
                                settings.javaScriptEnabled = false
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        super.onPageFinished(view, url.urlDecode())
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        if (url.contains("file://")) {
                            view.loadUrl(url)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW, url.urlDecode().toUri())
                            try {
                                ContextCompat.startActivity(context, intent, null)
                            } catch (e: ActivityNotFoundException) {
                                view.loadUrl(url)
                            }
                        }
                        return true
                    }
                }
            }
        },
        update = { webView -> webView.loadUrl(url.urlDecode()) }
    )
}