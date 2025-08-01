/*
 * This file is part of Neo Feed
 * Copyright (c) 2025   Neo Feed Team
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

package com.saulhdev.feeder.extensions

import android.util.Log
import com.rometools.modules.mediarss.MediaEntryModule
import com.rometools.modules.mediarss.MediaModule
import com.rometools.modules.mediarss.types.MediaContent
import com.rometools.modules.mediarss.types.MediaGroup
import com.rometools.modules.mediarss.types.Thumbnail
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEnclosure
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.feed.synd.SyndPerson
import com.saulhdev.feeder.data.entity.Attachment
import com.saulhdev.feeder.data.entity.Author
import com.saulhdev.feeder.data.entity.Item
import com.saulhdev.feeder.data.entity.JsonFeed
import com.saulhdev.feeder.utils.HtmlToPlainTextConverter
import com.saulhdev.feeder.utils.naiveFindImageLink
import com.saulhdev.feeder.utils.relativeLinkIntoAbsolute
import com.saulhdev.feeder.utils.relativeLinkIntoAbsoluteOrNull
import org.jsoup.parser.Parser.unescapeEntities
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.net.URL

suspend fun SyndFeed.asFeed(baseUrl: URL, feedIconFinder: suspend (URL) -> String?): JsonFeed {
    val feedAuthor: Author? = this.authors?.firstOrNull()?.asAuthor()

    val siteUrl = relativeLinkIntoAbsoluteOrNull(
        baseUrl,
        this.links?.firstOrNull {
            "alternate" == it.rel && "text/html" == it.type
        }?.href ?: this.link
    )

    // Base64 encoded images can be quite large - and crash database cursors
    val icon = image?.url?.let { url ->
        when {
            url.startsWith("data:") -> null
            else -> url
        }
    } ?: siteUrl?.let {
        feedIconFinder(URL(it))
    }

    try {
        return JsonFeed(
            title = plainTitle(),
            home_page_url = siteUrl,
            feed_url = relativeLinkIntoAbsoluteOrNull(
                baseUrl,
                this.links?.firstOrNull { "self" == it.rel }?.href
            ),
            description = this.description,
            icon = icon,
            author = feedAuthor,
            items = this.entries?.map { it.asItem(baseUrl = baseUrl, feedAuthor = feedAuthor) }
        )
    } catch (t: Throwable) {
        throw t
    }
}

fun SyndEntry.asItem(baseUrl: URL, feedAuthor: Author? = null): Item {
    try {
        val contentText = contentText().orIfBlank {
            mediaDescription() ?: ""
        }
        // Base64 encoded images can be quite large - and crash database cursors
        val image = thumbnail(baseUrl)?.let { img ->
            when {
                img.startsWith("data:") -> null
                else -> img
            }
        }
        val writer = when (author?.isNotBlank()) {
            true -> Author(name = author)
            else -> feedAuthor
        }

        return Item(
            id = relativeLinkIntoAbsoluteOrNull(baseUrl, this.uri),
            url = linkToHtml(baseUrl),
            title = plainTitle(),
            content_text = contentText,
            content_html = contentHtml(),
            summary = contentText.take(200),
            image = image,
            date_published = publishedRFC3339Date(),
            date_modified = modifiedRFC3339Date(),
            author = writer,
            attachments = enclosures?.map { it.asAttachment(baseUrl = baseUrl) }
        )
    } catch (t: Throwable) {
        throw t
    }
}

fun String.orIfBlank(block: () -> String): String =
    when (this.isBlank()) {
        true -> block()
        false -> this
    }

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.linkToHtml(feedBaseUrl: URL): String? {
    this.links?.firstOrNull { "alternate" == it.rel && "text/html" == it.type }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.links?.firstOrNull { "self" == it.rel && "text/html" == it.type }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.links?.firstOrNull { "self" == it.rel }?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it.href)
    }

    this.link?.let {
        return relativeLinkIntoAbsoluteOrNull(feedBaseUrl, it)
    }

    return null
}

fun SyndEnclosure.asAttachment(baseUrl: URL): Attachment {
    return Attachment(
        url = relativeLinkIntoAbsoluteOrNull(
            baseUrl,
            this.url
        ),
        mime_type = this.type,
        size_in_bytes = this.length
    )
}

fun SyndPerson.asAuthor(): Author {
    val url: String? = when {
        this.uri != null -> this.uri
        this.email != null -> "mailto:${this.email}"
        else -> null
    }
    return Author(
        name = this.name,
        url = url
    )
}

fun SyndEntry.contentText(): String {
    val possiblyHtml = when {
        !contents.isNullOrEmpty() -> { // Atom
            val contents = contents
            var possiblyHtml: String? = null

            for (c in contents) {
                if ("text" == c.type && c.value != null) {
                    return c.value
                } else if (null == c.type && c.value != null) {
                    // Suspect it might be text as per the Rome docs
                    // https://github.com/ralph-tice/rome/blob/master/src/main/java/com/sun/syndication/feed/synd/SyndContent.java
                    possiblyHtml = c.value
                    break
                } else if (("html" == c.type || "xhtml" == c.type) && c.value != null) {
                    possiblyHtml = c.value
                } else if (possiblyHtml == null && c.value != null) {
                    possiblyHtml = c.value
                }
            }

            possiblyHtml
        }

        else -> // Rss
            description?.value
    }

    val result = HtmlToPlainTextConverter().convert(possiblyHtml ?: "")

    // Description consists of at least one image, avoid opening browser for this item
    return when {
        result.isBlank() && possiblyHtml?.contains("img") == true ->
            "<image>"

        else -> result
    }
}

private fun convertAtomContentToPlainText(content: SyndContent?, fallback: String?): String {
    return HtmlToPlainTextConverter().convert(content?.value ?: fallback ?: "")
}

fun SyndFeed.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)
fun SyndEntry.plainTitle(): String = convertAtomContentToPlainText(titleEx, title)

fun SyndEntry.contentHtml(): String? {
    contents?.minByOrNull {
        when (it.type) {
            "xhtml", "html" -> 0
            else -> 1
        }
    }?.let {
        return it.value
    }

    return description?.value
}

fun SyndEntry.mediaDescription(): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?

    return media?.metadata?.description
        ?: media?.mediaContents?.firstOrNull {
            it.metadata?.description?.isNotBlank() == true
        }?.metadata?.description
        ?: media?.mediaGroups?.firstOrNull {
            it.metadata?.description?.isNotBlank() == true
        }?.metadata?.description
}

/**
 * Returns an absolute link, or null
 */
fun SyndEntry.thumbnail(feedBaseUrl: URL): String? {
    val media = this.getModule(MediaModule.URI) as MediaEntryModule?

    val thumbnailCandidates = sequence {
        media?.findThumbnailCandidates()?.let {
            yieldAll(it)
        }
        enclosures?.asSequence()
            ?.mapNotNull { it.findThumbnailCandidate() }
            ?.let {
                yieldAll(it)
            }
    }

    val thumbnail = thumbnailCandidates.maxByOrNull { it.width ?: -1 }

    return when {
        thumbnail != null -> relativeLinkIntoAbsolute(feedBaseUrl, thumbnail.url)
        else -> {
            val imgLink: String? =
                naiveFindImageLink(this.contentHtml())?.let { unescapeEntities(it, true) }
            // Now we are resolving against original, not the feed
            val siteBaseUrl: String? = this.linkToHtml(feedBaseUrl)

            try {
                when {
                    siteBaseUrl != null && imgLink != null -> relativeLinkIntoAbsolute(
                        URL(siteBaseUrl),
                        imgLink
                    )

                    imgLink != null -> relativeLinkIntoAbsolute(feedBaseUrl, imgLink)
                    else -> null
                }
            } catch (t: Throwable) {
                Log.e("FeederRomeExt", "Encountered some bad link: [$siteBaseUrl, $feedBaseUrl]", t)
                null
            }
        }
    }
}

data class ThumbnailCandidate(
    val width: Int?,
    val height: Int?,
    val url: String,
)

private fun MediaEntryModule.findThumbnailCandidates(): Sequence<ThumbnailCandidate> {
    return sequence {
        mediaContents?.forEach { mediaContent ->
            yieldAll(mediaContent.findThumbnailCandidates())
        }
        metadata?.thumbnail?.let { thumbnails ->
            yieldAll(
                thumbnails.mapNotNull { it.findThumbnailCandidate() }
            )
        }
        mediaGroups?.forEach { mediaGroup ->
            yieldAll(mediaGroup.findThumbnailCandidates())
        }
    }
}

private fun SyndEnclosure.findThumbnailCandidate(): ThumbnailCandidate? {
    if (type?.startsWith("image/") == true) {
        url?.let { url ->
            return ThumbnailCandidate(width = null, height = null, url = url)
        }
    }
    return null
}

private fun MediaGroup.findThumbnailCandidates(): Sequence<ThumbnailCandidate> = sequence {
    metadata.thumbnail?.forEach { thumbnail ->
        thumbnail.findThumbnailCandidate()?.let { thumbnailCandidate ->
            yield(thumbnailCandidate)
        }
    }
}

private fun Thumbnail.findThumbnailCandidate(): ThumbnailCandidate? {
    return url?.let { url ->
        ThumbnailCandidate(
            width = width,
            height = height,
            url = url.toString(),
        )
    }
}

private fun MediaContent.findThumbnailCandidates(): Sequence<ThumbnailCandidate> =
    sequence {
        metadata?.thumbnail?.forEach { thumbnail ->
            thumbnail.findThumbnailCandidate()?.let { thumbnailCandidate ->
                yield(thumbnailCandidate)
            }
        }

        if (isImage()) {
            reference?.let { ref ->
                yield(
                    ThumbnailCandidate(
                        width = width,
                        height = height,
                        url = ref.toString()
                    )
                )
            }
        }
    }

private fun MediaContent.isImage(): Boolean {
    return when {
        medium == "image" -> true
        pointsToImage(reference.toString()) -> true
        else -> false
    }
}

private fun pointsToImage(url: String): Boolean {
    return try {
        val u = URL(url)

        u.path.endsWith(".jpg", ignoreCase = true) ||
                u.path.endsWith(".jpeg", ignoreCase = true) ||
                u.path.endsWith(".gif", ignoreCase = true) ||
                u.path.endsWith(".png", ignoreCase = true) ||
                u.path.endsWith(".webp", ignoreCase = true)
    } catch (_: Exception) {
        false
    }
}

fun SyndEntry.publishedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (publishedDate != null) {
        true -> ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(publishedDate.time),
            ZoneOffset.systemDefault()
        )
        // This is the required element in atom feeds so it is a good fallback
        else -> modifiedRFC3339ZonedDateTime()
    }

fun SyndEntry.modifiedRFC3339ZonedDateTime(): ZonedDateTime? =
    when (updatedDate != null) {
        true -> ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(updatedDate.time),
            ZoneOffset.systemDefault()
        )

        else -> null
    }

fun SyndEntry.publishedRFC3339Date(): String? =
    publishedRFC3339ZonedDateTime()?.toString()

fun SyndEntry.modifiedRFC3339Date(): String? =
    modifiedRFC3339ZonedDateTime()?.toString()
