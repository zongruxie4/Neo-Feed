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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MinifluxUser(
    val id: Long = 0L,
    val username: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false,
)

@Serializable
data class MinifluxCategory(
    val id: Long = 0L,
    val title: String? = null,
    @SerialName("user_id") val userId: Long = 0L,
)

@Serializable
data class MinifluxFeed(
    val id: Long = 0L,
    @SerialName("user_id") val userId: Long = 0L,
    @SerialName("feed_url") val feedUrl: String? = null,
    @SerialName("site_url") val siteUrl: String? = null,
    val title: String? = null,
    @SerialName("checked_at") val checkedAt: String? = null,
    val disabled: Boolean = false,
    val category: MinifluxCategory? = null,
    val icon: JsonElement? = null,
)

@Serializable
data class MinifluxEntry(
    val id: Long = 0L,
    @SerialName("user_id") val userId: Long = 0L,
    @SerialName("feed_id") val feedId: Long = 0L,
    val status: String? = null,
    val hash: String? = null,
    val title: String? = null,
    val url: String? = null,
    @SerialName("comments_url") val commentsUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val content: String? = null,
    val author: String? = null,
    @SerialName("share_code") val shareCode: String? = null,
    val starred: Boolean = false,
    @SerialName("reading_time") val readingTime: Int = 0,
    val enclosures: List<MinifluxEnclosure>? = null,
    @SerialName("feed") val feed: MinifluxFeed? = null,
)

@Serializable
data class MinifluxEnclosure(
    val id: Long = 0L,
    @SerialName("user_id") val userId: Long = 0L,
    @SerialName("entry_id") val entryId: Long = 0L,
    val url: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    val size: Long = 0L,
)

@Serializable
data class MinifluxEntriesResponse(
    val total: Int = 0,
    val entries: List<MinifluxEntry> = emptyList(),
)

@Serializable
data class MinifluxUpdateEntriesRequest(
    @SerialName("entry_ids") val entryIds: List<Long>,
    val status: String,
)
