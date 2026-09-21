/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   Neo Feed Team
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

package com.saulhdev.feeder.manager.miniflux

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private const val TAG = "MinifluxClient"

class MinifluxClient(private val okHttpClient: OkHttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun buildBaseApiUrl(serverUrl: String): String {
        var base = serverUrl.trim().trimEnd('/')
        if (!base.startsWith("http://") && !base.startsWith("https://")) {
            base = "https://$base"
        }
        if (base.endsWith("/v1")) {
            return base
        }
        return "$base/v1"
    }

    suspend fun checkConnection(
        serverUrl: String,
        apiToken: String,
    ): Result<MinifluxUser> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/me"
        Log.d(TAG, "checkConnection GET $url")
        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, "checkConnection failed: $msg")
                    return@withContext Result.failure(IOException(msg))
                }
                val body = response.body.string()
                val user = json.decodeFromString<MinifluxUser>(body)
                Log.d(TAG, "checkConnection success for user=${user.username}")
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkConnection exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchCategories(
        serverUrl: String,
        apiToken: String,
    ): Result<List<MinifluxCategory>> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/categories"
        Log.d(TAG, "fetchCategories GET $url")
        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, "fetchCategories failed: $msg")
                    return@withContext Result.failure(IOException(msg))
                }
                val body = response.body.string()
                val categories = json.decodeFromString<List<MinifluxCategory>>(body)
                Log.d(TAG, "fetchCategories success: found ${categories.size} categories")
                Result.success(categories)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchCategories exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchFeeds(
        serverUrl: String,
        apiToken: String,
    ): Result<List<MinifluxFeed>> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/feeds"
        Log.d(TAG, "fetchFeeds GET $url")
        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, "fetchFeeds failed: $msg")
                    return@withContext Result.failure(IOException(msg))
                }
                val body = response.body.string()
                val feeds = json.decodeFromString<List<MinifluxFeed>>(body)
                Log.d(TAG, "fetchFeeds success: fetched ${feeds.size} feeds")
                Result.success(feeds)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchFeeds exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchEntries(
        serverUrl: String,
        apiToken: String,
        limit: Int = 200,
        offset: Int = 0,
        status: String? = null,
        afterEntryId: Long? = null,
    ): Result<MinifluxEntriesResponse> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val queryParams = mutableListOf<String>()
        queryParams.add("limit=$limit")
        queryParams.add("offset=$offset")
        queryParams.add("order=published_at")
        queryParams.add("direction=desc")
        if (!status.isNullOrBlank()) {
            queryParams.add("status=$status")
        }
        if (afterEntryId != null && afterEntryId > 0) {
            queryParams.add("after_entry_id=$afterEntryId")
        }

        val url = "$base/entries?${queryParams.joinToString("&")}"
        Log.d(TAG, "fetchEntries GET $url")
        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val msg = "HTTP ${response.code}: ${response.message}"
                    Log.e(TAG, "fetchEntries failed: $msg")
                    return@withContext Result.failure(IOException(msg))
                }
                val body = response.body.string()
                val entriesResponse = json.decodeFromString<MinifluxEntriesResponse>(body)
                Log.d(
                    TAG,
                    "fetchEntries success: total=${entriesResponse.total}, returned=${entriesResponse.entries.size}"
                )
                Result.success(entriesResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchEntries exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun markEntries(
        serverUrl: String,
        apiToken: String,
        entryIds: List<Long>,
        status: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (entryIds.isEmpty()) return@withContext Result.success(Unit)

        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/entries"
        val payload = MinifluxUpdateEntriesRequest(entryIds = entryIds, status = status)
        val requestBody = json.encodeToString(payload).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .put(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(
        serverUrl: String,
        apiToken: String,
        entryId: Long,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/entries/$entryId/bookmark"
        val requestBody = ByteArray(0).toRequestBody(null)

        val request = Request.Builder()
            .url(url)
            .header("X-Auth-Token", apiToken.trim())
            .put(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
