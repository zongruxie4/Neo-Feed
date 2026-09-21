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

package com.saulhdev.feeder.manager.nextcloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class NextcloudNewsClient(private val okHttpClient: OkHttpClient) {

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
        if (base.endsWith("/index.php/apps/news/api/v1-2")) {
            return base
        }
        if (base.endsWith("/apps/news/api/v1-2")) {
            return base
        }
        return "$base/index.php/apps/news/api/v1-2"
    }

    suspend fun checkConnection(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<NextcloudUser> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/user"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val body = response.body.string()
                val user = json.decodeFromString<NextcloudUser>(body)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFolders(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<List<NextcloudFolder>> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/folders"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val body = response.body.string()
                val foldersResponse = json.decodeFromString<NextcloudFoldersResponse>(body)
                Result.success(foldersResponse.folders)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFeeds(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<List<NextcloudFeed>> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/feeds"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val body = response.body.string()
                val feedsResponse = json.decodeFromString<NextcloudFeedsResponse>(body)
                Result.success(feedsResponse.feeds)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchItems(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        batchSize: Int = 100,
        getRead: Boolean = true,
        lastModified: Long = 0L,
    ): Result<List<NextcloudItem>> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/items?batchSize=$batchSize&getRead=$getRead&lastModified=$lastModified"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: ${response.message}")
                    )
                }
                val body = response.body.string()
                val itemsResponse = json.decodeFromString<NextcloudItemsResponse>(body)
                Result.success(itemsResponse.items)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markItemRead(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeEmptyPut("$serverUrl/items/$itemId/read", username, passwordOrToken)

    suspend fun markItemUnread(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeEmptyPut("$serverUrl/items/$itemId/unread", username, passwordOrToken)

    suspend fun starItem(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeEmptyPut("$serverUrl/items/$itemId/star", username, passwordOrToken)

    suspend fun unstarItem(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemId: Long,
    ): Result<Unit> = executeEmptyPut("$serverUrl/items/$itemId/unstar", username, passwordOrToken)

    suspend fun markMultipleRead(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemIds: List<Long>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/items/read/multiple"
        val payload = json.encodeToString(MultipleItemsRequest(itemIds))
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .put(payload.toRequestBody(jsonMediaType))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun starMultiple(
        serverUrl: String,
        username: String,
        passwordOrToken: String,
        itemIds: List<Long>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val base = buildBaseApiUrl(serverUrl)
        val url = "$base/items/star/multiple"
        val payload = json.encodeToString(MultipleItemsRequest(itemIds))
        val request = Request.Builder()
            .url(url)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .put(payload.toRequestBody(jsonMediaType))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun executeEmptyPut(
        endpointUrl: String,
        username: String,
        passwordOrToken: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val fullUrl = if (endpointUrl.contains("/api/v1-2/")) {
            endpointUrl
        } else {
            val base = buildBaseApiUrl(endpointUrl.substringBefore("/items/"))
            val path = endpointUrl.substringAfter("/items/")
            "$base/items/$path"
        }

        val request = Request.Builder()
            .url(fullUrl)
            .header("Authorization", Credentials.basic(username, passwordOrToken))
            .put("{}".toRequestBody(jsonMediaType))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
