/*
 * This file is part of Neo Feed
 * Copyright (c) 2024   Neo Applications Team
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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.components.ViewWithActionBar
import com.saulhdev.feeder.models.ExploreModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExplorePage() {
    val langs = arrayOf("ar-SA", "en-US", "es-SV", "fr-FR")
    val baseUrl = "https://raw.githubusercontent.com/saulhdev/NF-sources/main/feed_sources."
    var categories: ArrayList<String> by remember { mutableStateOf(arrayListOf()) }
    val feedSources: MutableState<ArrayList<ExploreModel>> =
        remember { mutableStateOf(arrayListOf()) }
    val selectedLangs: MutableState<List<String>> = remember { mutableStateOf(emptyList()) }
    val selectedCategories: MutableState<List<String>> = remember { mutableStateOf(emptyList()) }

    val scope = CoroutineScope(Dispatchers.IO)

    fun fetchCategories(selectedLangs: List<String>) {
        val client = OkHttpClient()
        categories.clear()
        feedSources.value = arrayListOf()
        scope.launch {
            for (lang in selectedLangs) {
                val sourceUrl = "$baseUrl$lang.json"
                val request = Request.Builder().url(sourceUrl)
                    .cacheControl(
                        CacheControl.Builder().maxAge(1, TimeUnit.MINUTES)
                            .build()
                    )
                    .build()
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body.string().let { responseBody ->
                        val sourcesArray = JSONObject(responseBody).getJSONArray("sources")
                        for (index in 0 until sourcesArray.length()) {
                            val source = sourcesArray.getJSONObject(index)
                            val tagArray = source.getJSONArray("tags")
                            val tagsList: ArrayList<String> = arrayListOf()
                            for (tagIndex in 0 until tagArray.length()) {
                                tagsList.add(tagArray.getString(tagIndex))
                            }
                            val model = ExploreModel(
                                title = source.getString("title"),
                                url = source.getString("url"),
                                language = source.getString("language"),
                                category = source.getString("category"),
                                tags = tagsList
                            )
                            feedSources.value.add(model)
                            categories.addAll(tagsList.toSet())
                        }
                    }
                }
            }
            if (categories.isNotEmpty()) {
                categories = categories.distinct().toMutableList() as ArrayList<String>
                categories.sort()
            }
        }
    }

    LaunchedEffect(selectedLangs.value) {
        if (selectedLangs.value.isNotEmpty()) {
            fetchCategories(selectedLangs.value)
        }
    }

    ViewWithActionBar(
        title = stringResource(id = R.string.explore)
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(start = 8.dp, top = paddingValues.calculateTopPadding())
        ) {
            Text(
                stringResource(R.string.explore_langs),
                style = MaterialTheme.typography.titleMedium
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                langs.forEachIndexed { index, lang ->
                    FilterChip(
                        onClick = {
                            selectedLangs.value = selectedLangs.value.toMutableList().apply {
                                if (contains(lang)) {
                                    remove(lang)
                                } else {
                                    add(lang)
                                }
                            }
                        },
                        label = {
                            Text(lang)
                        },
                        selected = selectedLangs.value.contains(lang),
                        leadingIcon = {}
                    )
                    if (index < langs.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.explore_categories),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                categories.forEachIndexed { index, category ->
                    FilterChip(
                        onClick = {
                            selectedCategories.value =
                                selectedCategories.value.toMutableList().apply {
                                    if (contains(category)) {
                                        remove(category)
                                    } else {
                                        add(category)
                                    }
                                }
                        },
                        label = {
                            Text(category)
                        },
                        selected = selectedCategories.value.contains(category),
                        leadingIcon = {}
                    )
                    if (index < categories.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

            }
        }
    }
}
