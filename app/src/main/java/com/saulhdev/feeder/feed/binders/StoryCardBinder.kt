package com.saulhdev.feeder.feed.binders

import android.text.Html
import android.view.View
import coil.load
import com.saulhdev.feeder.ComposeActivity
import com.saulhdev.feeder.databinding.FeedCardStoryLargeBinding
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.utils.RelativeTimeHelper
import com.saulhdev.feeder.utils.launchView
import com.saulhdev.feeder.utils.urlEncode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.content.StoryCardContent

object StoryCardBinder : FeedBinder {
    override fun bind(item: FeedItem, view: View) {
        val content = item.content as StoryCardContent
        val binding = FeedCardStoryLargeBinding.bind(view)
        val prefs = FeedPreferences(view.context)
        binding.storyTitle.text = content.title
        binding.storySource.text = content.source.title
        binding.storyDate.text =
            RelativeTimeHelper.getDateFormattedRelative(view.context, (item.time / 1000) - 1000)

        if (content.text.isEmpty()) {
            binding.storyDesc.visibility = View.GONE
        } else {
            binding.storyDesc.text = Html.fromHtml(content.text, 0).toString()
        }

        if (
            content.background_url.isEmpty() ||
            content.background_url == "null" ||
            content.background_url.contains(".rss")
        ) {
            binding.storyPic.visibility = View.GONE
        } else {
            binding.storyPic.visibility = View.VISIBLE
            binding.storyPic.load(content.background_url) {
                crossfade(true)
                crossfade(500)
            }
        }

        binding.root.setOnClickListener {
            if (prefs.openInBrowser.onGetValue()) {
                view.context.launchView(content.link)
            } else {
                val scope = CoroutineScope(Dispatchers.Main)

                scope.launch {
                    if (prefs.offlineReader.onGetValue()) {
                        view.context.startActivity(
                            ComposeActivity.createIntent(
                                view.context,
                                "article_page/${item.id}/"
                            )
                        )
                    } else {
                        view.context.startActivity(
                            ComposeActivity.createIntent(
                                view.context,
                                "web_view/${content.link.urlEncode()}/"
                            )
                        )
                    }
                }
            }
        }
    }
}