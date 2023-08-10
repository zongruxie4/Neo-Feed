package com.saulhdev.feeder.feed.binders

import android.os.Build
import android.text.Html
import android.util.SparseIntArray
import android.view.View
import coil.load
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.compose.pages.openLinkInCustomTab
import com.saulhdev.feeder.databinding.FeedCardStoryLargeBinding
import com.saulhdev.feeder.db.ArticleRepository
import com.saulhdev.feeder.preference.FeedPreferences
import com.saulhdev.feeder.sdk.FeedItem
import com.saulhdev.feeder.theme.Theming
import com.saulhdev.feeder.utils.RelativeTimeHelper
import com.saulhdev.feeder.utils.isDark
import com.saulhdev.feeder.utils.launchView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object StoryCardBinder : FeedBinder {
    override fun bind(theme: SparseIntArray?, item: FeedItem, view: View) {
        val context = view.context
        val content = item.content
        val binding = FeedCardStoryLargeBinding.bind(view)
        val prefs = FeedPreferences.getInstance(context)
        val repository = ArticleRepository(context)
        var bookmarked = item.bookmarked
        binding.storyTitle.text = content.title
        binding.storySource.text = content.source.title
        binding.storyDate.text =
            RelativeTimeHelper.getDateFormattedRelative(view.context, (item.time / 1000) - 1000)

        if (content.text.isEmpty()) {
            binding.storySummary.visibility = View.GONE
        } else {
            binding.storySummary.text = Html.fromHtml(content.text, 0).toString()
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

        if (bookmarked) {
            binding.saveButton.setImageResource(R.drawable.ic_heart_fill)
        } else {
            binding.saveButton.setImageResource(R.drawable.ic_heart)
        }

        binding.saveButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                repository.bookmarkArticle(item.id, !bookmarked)
                bookmarked = !bookmarked
                if (bookmarked) {
                    binding.saveButton.setImageResource(R.drawable.ic_heart_fill)
                } else {
                    binding.saveButton.setImageResource(R.drawable.ic_heart)
                }
            }
        }

        binding.root.setOnClickListener {
            if (prefs.openInBrowser.getValue()) {
                view.context.launchView(content.link)
            } else {
                val scope = CoroutineScope(Dispatchers.Main)
                val toolbarColor = if (Build.VERSION.SDK_INT > 30) {
                    context.getColor(com.google.android.material.R.color.m3_sys_color_dynamic_primary_fixed)
                } else {
                    context.getColor(R.color.textColorPrimary)
                }

                scope.launch {
                    if (prefs.offlineReader.getValue()) {
                        view.context.startActivity(
                            MainActivity.createIntent(
                                view.context,
                                "article_page/${item.id}/"
                            )
                        )
                    } else {
                        openLinkInCustomTab(
                            context,
                            item.content.link,
                            toolbarColor
                        )
                    }
                }
            }
        }

        theme ?: return
        binding.cardStory.setBackgroundColor(theme.get(Theming.Colors.CARD_BG.ordinal))
        val themeCard = if (theme.get(Theming.Colors.CARD_BG.ordinal)
                .isDark()
        ) Theming.defaultDarkThemeColors else Theming.defaultLightThemeColors
        binding.storyTitle.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
        binding.storySource.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
        binding.storyDate.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
        binding.storySummary.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
    }
}