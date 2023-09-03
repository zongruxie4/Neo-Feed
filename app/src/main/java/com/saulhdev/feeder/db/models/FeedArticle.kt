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

package com.saulhdev.feeder.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.saulhdev.feeder.db.ID_UNSET
import com.saulhdev.feeder.utils.Item
import com.saulhdev.feeder.utils.JsonFeed
import com.saulhdev.feeder.utils.relativeLinkIntoAbsolute
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import com.saulhdev.feeder.views.HtmlToPlainTextConverter
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
    var id: Long = ID_UNSET,
    var guid: String = "",
    var title: String = "",
    var plainTitle: String = "",
    var imageUrl: String? = null,
    var enclosureLink: String? = null,
    var plainSnippet: String = "",
    var description: String = "",
    var content_html: String = "",
    var author: String? = "",
    var pubDate: ZonedDateTime? = null,
    var link: String? = "",
    var feedId: Long = 0,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER) var firstSyncedTime: Instant = Instant.EPOCH,
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER) var primarySortTime: Instant = Instant.EPOCH,
    var categories: ArrayList<String> = arrayListOf(),
    var pinned: Boolean = false,
    var bookmarked: Boolean = false,
) {
    fun updateFromParsedEntry(
        entry: Item,
        entryGuid: String,
        feed: JsonFeed,
    ) {
        val converter = HtmlToPlainTextConverter()
        // Be careful about nulls.
        val text = entry.content_html ?: entry.content_text ?: ""
        val summary: String = (
                entry.summary ?: entry.content_text
                ?: converter.convert(text)
                ).take(200)

        // Make double sure no base64 images are used as thumbnails
        val safeImage = when {
            entry.image?.startsWith("data") == true -> null
            else -> entry.image
        }

        val absoluteImage = when {
            feed.feed_url != null && safeImage != null -> {
                relativeLinkIntoAbsolute(sloppyLinkToStrictURL(feed.feed_url.toString()), safeImage)
            }

            else -> safeImage
        }

        this.guid = entryGuid
        entry.title?.let { this.plainTitle = it.take(200) }
        this.title = this.plainTitle
        this.plainSnippet = summary

        this.imageUrl = absoluteImage
        this.enclosureLink = entry.attachments?.firstOrNull()?.url
        this.author = entry.author?.name ?: feed.author?.name
        this.link = entry.url

        this.pubDate =
            try {
                // Allow an actual pubdate to be updated
                ZonedDateTime.parse(entry.date_published)
            } catch (t: Throwable) {
                // If a pubdate is missing, then don't update if one is already set
                this.pubDate ?: ZonedDateTime.now(ZoneOffset.UTC)
            }
        primarySortTime = minOf(firstSyncedTime, pubDate?.toInstant() ?: firstSyncedTime)
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
