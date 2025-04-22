package com.saulhdev.feeder.utils

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.db.FeedRepository
import com.saulhdev.feeder.db.models.Feed
import com.saulhdev.feeder.sdk.FeedCategory
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.sdk.IFeedInterface
import com.saulhdev.feeder.sdk.IFeedInterfaceCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class HFPluginService : Service(), CoroutineScope by MainScope() {
    private val feedsRepo: FeedRepository by inject(FeedRepository::class.java)
    private val articlesRepo: ArticleRepository by inject(ArticleRepository::class.java)

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
                    val feedList: List<Feed> = feedsRepo.loadFeeds()

                    feedList.forEach { feed ->
                        articlesRepo.getFeedArticles(feed).forEach { article ->
                            list.add(FeedItem(article, feed))
                        }
                    }
                }

                callback.onFeedReceive(list) // TODO Simplify
            }
        }

        override fun getCategories(callback: IFeedInterfaceCallback) {
            val feedList: List<Feed> = feedsRepo.loadFeeds()

            callback.onCategoriesReceive(feedList.map {
                FeedCategory(it.id.toString(), it.title, Color.GREEN, it.feedImage.toString())
            })
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }
}