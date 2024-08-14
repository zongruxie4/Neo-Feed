package com.saulhdev.feeder.utils

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.models.StoryCardContent
import com.saulhdev.feeder.sdk.FeedCategory
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sdk.FeedItemType
import com.saulhdev.feeder.sdk.IFeedInterface
import com.saulhdev.feeder.sdk.IFeedInterfaceCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import org.koin.java.KoinJavaComponent.inject

class HFPluginService : Service(), CoroutineScope by MainScope() {
    private val repository: ArticleRepository by inject(ArticleRepository::class.java)

    private val mBinder: IBinder = object : IFeedInterface.Stub() {
        override fun getFeed(
            callback: IFeedInterfaceCallback?,
            page: Int,
            category_id: String,
            parameters: Bundle?
        ) {
            callback ?: return
            launch {
                val list = mutableListOf<FeedItem>()

                //Load feed articles from database
                withContext(Dispatchers.IO) {
                    val feedList: List<Feed> = repository.getAllFeeds()

                    feedList.forEach { feed ->
                        repository.getFeedArticles(feed).forEach { article ->
                            list.add(
                                FeedItem(
                                    id = article.id,
                                    title = "${feed.title} [RSS]",
                                    type = FeedItemType.STORY_CARD,
                                    content = StoryCardContent(
                                        title = article.title,
                                        text = article.description,
                                        background_url = article.imageUrl ?: "",
                                        link = article.link ?: "",
                                        source = FeedCategory(
                                            feed.url.toString(),
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
                            )
                        }

                    }
                }

                callback.onFeedReceive(list)
            }
        }

        override fun getCategories(callback: IFeedInterfaceCallback) {
            val feedList: List<Feed> = repository.getAllFeeds()

            callback.onCategoriesReceive(feedList.map {
                FeedCategory(it.url.toString(), it.title, Color.GREEN, it.feedImage.toString())
            })
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }
}