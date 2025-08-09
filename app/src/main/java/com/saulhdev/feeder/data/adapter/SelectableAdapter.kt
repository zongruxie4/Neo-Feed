package com.saulhdev.feeder.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.saulhdev.feeder.R

class SelectableAdapter(val context: Context) :
    RecyclerView.Adapter<SelectableAdapter.ViewHolder>() {
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

    fun toggleSelection(item: String) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.chip.text = item
        val isSelected = selectedItems.contains(item)
        holder.chip.isChecked = isSelected

        holder.itemView.setOnClickListener {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            notifyItemChanged(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chip: Chip = itemView.findViewById(R.id.chip_item)
    }
}