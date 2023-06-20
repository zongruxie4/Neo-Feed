package com.saulhdev.feeder.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.R
import com.saulhdev.feeder.feed.binders.StoryCardBinder
import com.saulhdev.feeder.feed.binders.TextCardBinder
import com.saulhdev.feeder.feed.binders.TextCardWithActionsBinder
import ua.itaysonlab.hfsdk.FeedItem
import ua.itaysonlab.hfsdk.FeedItemType

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    private var list = listOf<FeedItem>()
    private lateinit var layoutInflater: LayoutInflater

    fun replace(new: List<FeedItem>) {
        list = new
        notifyDataSetChanged()
    }

    fun setTheme() {
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return (list[position].type.ordinal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        if (!::layoutInflater.isInitialized) layoutInflater = LayoutInflater.from(parent.context)

        val layoutResource = when (FeedItemType.values()[viewType]) {
            FeedItemType.TEXT_CARD -> R.layout.notification_simple
            FeedItemType.TEXT_CARD_ACTIONS -> R.layout.feed_card_text
            FeedItemType.STORY_CARD -> R.layout.feed_card_story_large
        }

        return FeedViewHolder(viewType, layoutInflater.inflate(layoutResource, parent, false))
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = list[position]
        when (FeedItemType.values()[holder.type]) {
            FeedItemType.TEXT_CARD -> TextCardBinder.bind(item, holder.itemView)
            FeedItemType.TEXT_CARD_ACTIONS -> TextCardWithActionsBinder.bind(item, holder.itemView)
            FeedItemType.STORY_CARD -> StoryCardBinder.bind(item, holder.itemView)
        }
    }

    inner class FeedViewHolder(val type: Int, itemView: View) : RecyclerView.ViewHolder(itemView)
}