package com.saulhdev.feeder.ui.feed.binders

import android.content.Intent
import android.content.res.ColorStateList
import android.text.Html
import android.util.SparseIntArray
import android.view.View
import coil.load
import com.google.android.material.button.MaterialButton
import com.saulhdev.feeder.MainActivity
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.content.FeedPreferences
import com.saulhdev.feeder.data.entity.FeedItem
import com.saulhdev.feeder.data.repository.ArticleRepository
import com.saulhdev.feeder.databinding.FeedCardStoryLargeBinding
import com.saulhdev.feeder.ui.compose.theme.Theming
import com.saulhdev.feeder.ui.navigation.Routes
import com.saulhdev.feeder.utils.RelativeTimeHelper
import com.saulhdev.feeder.utils.extensions.isDark
import com.saulhdev.feeder.utils.extensions.launchView
import com.saulhdev.feeder.utils.openLinkInCustomTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject

object StoryCardBinder : FeedBinder {
    override fun bind(theme: SparseIntArray?, item: FeedItem, view: View) {
        val context = view.context
        val content = item.content
        val binding = FeedCardStoryLargeBinding.bind(view)
        val prefs = get<FeedPreferences>(FeedPreferences::class.java)
        val repository: ArticleRepository by inject(ArticleRepository::class.java)
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

        updateSaveIcon(binding.saveButton, bookmarked)
        binding.saveButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                repository.bookmarkArticle(item.id, !bookmarked)
                bookmarked = !bookmarked
                updateSaveIcon(binding.saveButton, bookmarked)
            }
        }

        binding.shareButton.setOnClickListener {
            val intent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, content.link)
                    putExtra(Intent.EXTRA_TITLE, content.title)
                    type = "text/plain"
                },
                null,
            )
            context.startActivity(intent)
        }

        binding.root.setOnClickListener {
            if (prefs.openInBrowser.getValue()) {
                view.context.launchView(content.link)
            } else {
                val scope = CoroutineScope(Dispatchers.Main)

                scope.launch {
                    if (prefs.offlineReader.getValue()) {
                        view.context.startActivity(
                            MainActivity.navigateIntent(
                                view.context,
                                "${Routes.ARTICLE_VIEW}/${item.id}"
                            )
                        )
                    } else {
                        scope.launch {
                            if (prefs.offlineReader.getValue()) {
                                view.context.startActivity(
                                    MainActivity.navigateIntent(
                                        context,
                                        "${Routes.ARTICLE_VIEW}/${item.id}"
                                    )
                                )
                            } else {
                                openLinkInCustomTab(
                                    context,
                                    content.link
                                )
                            }
                        }
                    }
                }
            }
        }

        theme ?: return
        binding.cardStory.setCardBackgroundColor(ColorStateList.valueOf(theme.get(Theming.Colors.CARD_BG.ordinal)))
        val themeCard = if (theme.get(Theming.Colors.CARD_BG.ordinal).isDark())
            Theming.defaultDarkThemeColors
        else
            Theming.defaultLightThemeColors
        binding.storyTitle.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
        binding.storySource.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
        binding.storyDate.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
        binding.storySummary.setTextColor(themeCard.get(Theming.Colors.TEXT_COLOR_SECONDARY.ordinal))
        binding.shareButton.iconTint =
            ColorStateList.valueOf(themeCard.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
        binding.saveButton.iconTint =
            ColorStateList.valueOf(themeCard.get(Theming.Colors.TEXT_COLOR_PRIMARY.ordinal))
    }

    private fun updateSaveIcon(button: MaterialButton, bookmarked: Boolean) {
        if (bookmarked) {
            button.setIconResource(R.drawable.ic_heart_fill)
        } else {
            button.setIconResource(R.drawable.ic_heart)
        }
    }
}