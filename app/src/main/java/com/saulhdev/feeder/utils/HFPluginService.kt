package com.saulhdev.feeder.utils

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import com.prof.rssparser.Parser
import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.preference.FeedPreferences
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import ua.itaysonlab.hfsdk.*
import ua.itaysonlab.hfsdk.content.StoryCardContent
import java.text.SimpleDateFormat
import java.util.*

/**
 * Your most important class (maybe)
 * This defines which data does the plugin send to HomeFeeder.
 */
class HFPluginService : Service(), CoroutineScope by MainScope() {
    val sourceSdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

    private val mBinder = object : IFeedInterface.Stub() {
        override fun getFeed(
            callback: IFeedInterfaceCallback?,
            page: Int,
            category_id: String,
            parameters: Bundle?
        ) {
            callback ?: return

            launch {
                val list = mutableListOf<FeedItem>()

                withContext(Dispatchers.IO) {
                    val parser = Parser(OkHttpClient())
                    val prefs = FeedPreferences(this@HFPluginService)
                    val feedList =
                        prefs.feedList.onGetValue().map { SavedFeedModel(JSONObject(it)) }
                    feedList.forEach { model ->
                        parser.getChannel(model.feedUrl).articles.forEach { article ->
                            list.add(
                                FeedItem(
                                    "${model.name} [RSS]",
                                    FeedItemType.STORY_CARD,
                                    StoryCardContent(
                                        title = article.title!!,
                                        text = article.description!!,
                                        background_url = article.image ?: "",
                                        link = article.link ?: "",
                                        source = FeedCategory(
                                            model.feedUrl,
                                            model.name,
                                            Color.GREEN,
                                            model.feedImage
                                        )
                                    ),
                                    sourceSdf.parse(article.pubDate!!)!!.time
                                )
                            )
                        }
                    }
                }

                callback.onFeedReceive(list)
            }
        }

        override fun getCategories(callback: IFeedInterfaceCallback) {
            val prefs = FeedPreferences(this@HFPluginService)
            val feedList = prefs.feedList.onGetValue().map { SavedFeedModel(JSONObject(it)) }
            callback.onCategoriesReceive(feedList.map {
                FeedCategory(it.feedUrl, it.name, Color.GREEN, it.feedImage)
            })
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }
}