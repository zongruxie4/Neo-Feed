package com.saulhdev.feeder.data.db.models

import android.graphics.Color
import androidx.room.Embedded
import androidx.room.Relation
import com.saulhdev.feeder.data.entity.FeedCategory
import com.saulhdev.feeder.manager.models.StoryCardContent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

data class FeedItem(
    @Embedded
    val article: FeedArticle,

    @Relation(
        parentColumn = "feedId",
        entityColumn = "id"
    )
    val feed: Feed
) {
    fun toStoryCardContent(): StoryCardContent {
        return StoryCardContent(
            title = contentTitle,
            text = article.description,
            backgroundUrl = article.imageUrl ?: "",
            tag = feedTag,
            link = link,
            source = FeedCategory(
                sourceId,
                feedTitle,
                Color.GREEN,
                feed.feedImage.toString()
            )
        )
    }

    val id: Long
        get() = article.id

    val link: String
        get() = article.link ?: ""

    val sourceId: String
        get() = feed.id.toString()

    val feedTitle: String
        get() = feed.title

    val displayTitle: String
        get() = "${feed.title} [RSS]"

    val contentTitle: String
        get() = article.title

    val timeMillis: Long
        get() = article.pubDate?.let { pubDate ->
            Date.from(
                ZonedDateTime.parse(
                    pubDate.toString(),
                    DateTimeFormatter.ISO_ZONED_DATE_TIME
                ).toInstant()
            ).time
        } ?: 0L

    val bookmarked: Boolean
        get() = article.bookmarked

    val pinned: Boolean
        get() = article.pinned

    val feedTag: String
        get() = feed.tag

    val domain: String?
        get() = article.domain

    val enclosureFilename: String?
        get() = article.enclosureFilename
}