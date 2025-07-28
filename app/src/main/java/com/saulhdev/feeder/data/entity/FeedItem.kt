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

package com.saulhdev.feeder.data.entity

import android.graphics.Color
import android.os.Parcelable
import com.saulhdev.feeder.data.db.models.Feed
import com.saulhdev.feeder.data.db.models.FeedArticle
import com.saulhdev.feeder.manager.models.StoryCardContent
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

@Parcelize
data class FeedItem(
    val id: Long,
    val title: String,
    val type: FeedItemType,
    val content: StoryCardContent,
    val bookmarked: Boolean,
    val time: Long
) : Parcelable {
    constructor(
        article: FeedArticle,
        feed: Feed,
    ) : this(
        id = article.id,
        title = "${feed.title} [RSS]",
        type = FeedItemType.STORY_CARD,
        content = StoryCardContent(
            title = article.title,
            text = article.description,
            backgroundUrl = article.imageUrl ?: "",
            tag = feed.tag,
            link = article.link ?: "",
            source = FeedCategory(
                feed.id.toString(),
                feed.title,
                Color.GREEN,
                feed.feedImage.toString()
            )
        ),
        bookmarked = article.bookmarked,
        time = Date.from(
            ZonedDateTime.parse(
                article.pubDate.toString(),
                DateTimeFormatter.ISO_ZONED_DATE_TIME
            ).toInstant()
        ).time
    )

    override fun describeContents(): Int {
        return 0
    }
}