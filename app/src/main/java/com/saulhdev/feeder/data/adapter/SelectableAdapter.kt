package com.saulhdev.feeder.data.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.R

class SelectableAdapter : RecyclerView.Adapter<SelectableAdapter.ViewHolder>() {
    private var items = listOf<String>()
    private var selectedItems = arrayListOf<String>()

    fun replace(items: List<String>, selectedItems: ArrayList<String>) {
        if (this.items != items) {
            this.items = items
        }
        if (this.selectedItems != selectedItems) {
            this.selectedItems = selectedItems
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getSelectedItems(): List<String> {
        return selectedItems
    }

    fun getSelectedPositions(): List<Int> {
        return items.mapIndexedNotNull { index, item ->
            if (selectedItems.contains(item)) index else null
        }
    }

    fun clearSelections() {
        val selectedPositions = items.mapIndexedNotNull { index, item ->
            if (selectedItems.contains(item)) index else null
        }
        selectedItems = arrayListOf()
        selectedPositions.forEach { notifyItemChanged(it) }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_selectable, parent, false)
        return ViewHolder(view)
    }

    private fun setIcon(holder: ViewHolder, isSelected: Boolean) {
        if (isSelected) {
            holder.iconCheckmark.setImageResource(R.drawable.ic_check_24)
        } else {
            holder.iconCheckmark.setImageResource(R.drawable.ic_circle_24dp)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        val isSelected = selectedItems.contains(item)
        setIcon(holder, isSelected)
        holder.itemView.setOnClickListener {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            setIcon(holder, !isSelected)
            notifyItemChanged(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text_item)
        val iconCheckmark: ImageView = itemView.findViewById(R.id.icon_checkmark)
    }
}