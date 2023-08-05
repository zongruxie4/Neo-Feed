package com.saulhdev.feeder.feed

import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.R
import com.saulhdev.feeder.feed.binders.StoryCardBinder
import com.saulhdev.feeder.sdk.FeedItem

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    private var list = listOf<FeedItem>()
    private lateinit var layoutInflater: LayoutInflater
    private var theme: SparseIntArray? = null

    fun replace(new: List<FeedItem>) {
        list = new
        notifyDataSetChanged()
    }

    fun setTheme(theme: SparseIntArray) {
        this.theme = theme
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

        val layoutResource = R.layout.feed_card_story_large

        return FeedViewHolder(viewType, layoutInflater.inflate(layoutResource, parent, false))
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = list[position]
        StoryCardBinder.bind(theme, item, holder.itemView)
    }

    inner class FeedViewHolder(val type: Int, itemView: View) : RecyclerView.ViewHolder(itemView)
}