package ua.itaysonlab.homefeeder.overlay.feed.binders

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.SparseIntArray
import android.view.View
import androidx.annotation.RequiresApi
import coil.load
import com.saulhdev.feeder.databinding.FeedCardStoryLargeBinding
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.content.StoryCardContent

object StoryCardBinder: FeedBinder {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun bind(theme: SparseIntArray?, item: FeedItem, view: View) {
        val content = item.content as StoryCardContent
        val binding = FeedCardStoryLargeBinding.bind(view)

        binding.storyTitle.text = content.title
        binding.storySource.text = content.source.title
        binding.storyDate.text = item.time.toString()

        if (content.text.isEmpty()) {
            binding.storyDesc.visibility = View.GONE
        } else {
            binding.storyDesc.text = Html.fromHtml(content.text, 0).toString()
        }

        binding.storyPic.load(content.background_url)

        binding.root.setOnClickListener {
            view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content.link)))
        }
    }
}