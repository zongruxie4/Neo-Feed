/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.saulhdev.feeder.data.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.saulhdev.feeder.data.db.ID_UNSET
import com.saulhdev.feeder.data.entity.Item
import com.saulhdev.feeder.data.entity.JsonFeed
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import com.saulhdev.feeder.utils.relativeLinkIntoAbsolute
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.net.URI
import java.net.URL

@Entity(
    tableName = "FeedArticle",
    indices = [
        Index(value = ["id", "feedId"], unique = true),
        Index(value = ["feedId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = CASCADE
        )
    ]
)
data class FeedArticle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = ID_UNSET,
    val guid: String = "",
    val title: String = "",
    val plainTitle: String = "",
    val imageUrl: String? = null,
    val enclosureLink: String? = null,
    val plainSnippet: String = "",
    val description: String = "",
    val content_html: String = "",
    val author: String? = "",
    val pubDate: ZonedDateTime? = null,
    val link: String? = "",
    val feedId: Long = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val firstSyncedTime: Instant = Instant.EPOCH,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val primarySortTime: Instant = Instant.EPOCH,
    val categories: ArrayList<String> = arrayListOf(),
    val pinned: Boolean = false,
    val bookmarked: Boolean = false,
) {
    fun updateFromParsedEntry(
        entry: Item,
        entryGuid: String,
        feed: JsonFeed,
        feedId: Long,
    ): FeedArticle {
        val converter = HtmlToPlainTextConverter()
        // Be careful about nulls.
        val text = entry.content_html ?: entry.content_text ?: ""
        val summary: String = (
                entry.summary ?: entry.content_text
                ?: converter.convert(text)
                ).take(200)

        // Make double sure no base64 images are used as thumbnails
        val safeImage = when {
            entry.image?.startsWith("data") == true
                 -> null

            else -> entry.image
        }

        val absoluteImage = when {
            feed.feed_url != null && safeImage != null
                 -> relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feed.feed_url), safeImage)

            else -> safeImage
        }

        return copy(
            guid = entryGuid,
            plainTitle = entry.title?.take(200) ?: this.plainTitle,
            title = this.plainTitle,
            plainSnippet = summary,
            imageUrl = absoluteImage,
            enclosureLink = entry.attachments?.firstOrNull()?.url,
            author = entry.author?.name ?: feed.author?.name,
            link = entry.url,
            pubDate = try {
                // Allow an actual pubdate to be updated
                ZonedDateTime.parse(entry.date_published)
            } catch (_: Throwable) {
                // If a pubdate is missing, then don't update if one is already set
                this.pubDate ?: ZonedDateTime.now(ZoneOffset.UTC)
            },
            primarySortTime = minOf(firstSyncedTime, pubDate?.toInstant() ?: firstSyncedTime),
            feedId = feedId,
        )
    }

    val enclosureFilename: String?
        get() {
            enclosureLink?.let { enclosureLink ->
                var fname: String? = null
                try {
                    fname = URI(enclosureLink).path.split("/").last()
                } catch (_: Exception) {
                }
                return if (fname.isNullOrEmpty()) {
                    null
                } else {
                    fname
                }
            }
            return null
        }

    val domain: String?
        get() {
            val l: String? = enclosureLink ?: link
            if (l != null) {
                try {
                    return URL(l).host.replace("www.", "")
                } catch (_: Throwable) {
                }
            }
            return null
        }
}

interface FeedItemForFetching {
    val id: Long
    val link: String?
}
