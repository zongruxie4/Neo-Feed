package com.saulhdev.feeder.feed.binders

import android.text.Html
import android.util.SparseIntArray
import android.view.View
import coil.load
import com.saulhdev.feeder.ComposeActivity
import com.saulhdev.feeder.databinding.FeedCardStoryLargeBinding
import com.saulhdev.feeder.utils.RelativeTimeHelper
import com.saulhdev.feeder.utils.urlEncode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.content.StoryCardContent

object StoryCardBinder: FeedBinder {
    override fun bind(theme: SparseIntArray?, item: FeedItem, view: View) {
        val content = item.content as StoryCardContent
        val binding = FeedCardStoryLargeBinding.bind(view)

        binding.storyTitle.text = content.title
        binding.storySource.text = content.source.title
        binding.storyDate.text =
            RelativeTimeHelper.getDateFormattedRelative(view.context, (item.time / 1000) - 1000)

        if (content.text.isEmpty()) {
            binding.storyDesc.visibility = View.GONE
        } else {
            binding.storyDesc.text = Html.fromHtml(content.text, 0).toString()
        }

        binding.storyPic.load(content.background_url)

        binding.root.setOnClickListener {
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
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