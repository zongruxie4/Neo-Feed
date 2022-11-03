package com.saulhdev.feeder.utils

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.saulhdev.feeder.db.Feed
import com.saulhdev.feeder.db.FeedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.itaysonlab.hfsdk.FeedCategory
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.FeedItemType
import ua.itaysonlab.hfsdk.IFeedInterface
import ua.itaysonlab.hfsdk.IFeedInterfaceCallback
import ua.itaysonlab.hfsdk.content.StoryCardContent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class HFPluginService : Service(), CoroutineScope by MainScope() {

    private val mBinder = object : IFeedInterface.Stub() {
        @RequiresApi(Build.VERSION_CODES.O)
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
                    val repository = FeedRepository(this@HFPluginService)
                    val feedList: List<Feed> = repository.getAllFeeds()

                    feedList.forEach { feed ->
                        repository.getFeedArticles(feed).forEach { article ->
                            list.add(
                                FeedItem(
                                    "${feed.title} [RSS]",
                                    FeedItemType.STORY_CARD,
                                    StoryCardContent(
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
                                    Date.from(
                                        ZonedDateTime.parse(
                                            article.pubDate,
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
            val repository = FeedRepository(this@HFPluginService)
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