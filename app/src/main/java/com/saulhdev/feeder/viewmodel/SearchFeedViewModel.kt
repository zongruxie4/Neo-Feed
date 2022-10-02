package com.saulhdev.feeder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.saulhdev.feeder.models.FeedParser
import com.saulhdev.feeder.models.SavedFeedModel
import com.saulhdev.feeder.utils.sloppyLinkToStrictURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import java.net.URL

class SearchFeedViewModel : ViewModel() {
    private val feedParser: FeedParser = FeedParser()

    fun searchForFeeds(url: URL) =
        flow {
            emit(url)
            feedParser.getAlternateFeedLinksAtUrl(url)
                .forEach {
                    emit(sloppyLinkToStrictURL(it.first))
                }
        }.mapNotNull {
            try {
                feedParser.parseFeedUrl(it)?.let { feed ->
                    SavedFeedModel(
                        title = feed.title ?: "",
                        url = feed.feed_url ?: it.toString(),
                        description = feed.description ?: "",
                        isError = false
                    )
                }
            } catch (t: Throwable) {
                Log.e("searchForFeeds", "Failed to parse", t)
                SavedFeedModel(
                    title = FAILED_TO_PARSE_PLACEHOLDER,
                    url = it.toString(),
                    description = t.message ?: "",
                    isError = true
                )
            }
        }
            .flowOn(Dispatchers.Default)

    companion object {
        const val FAILED_TO_PARSE_PLACEHOLDER = "failed_to_parse"
    }
}