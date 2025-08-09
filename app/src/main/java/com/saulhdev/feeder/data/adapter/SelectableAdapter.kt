package com.saulhdev.feeder.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saulhdev.feeder.databinding.ItemListSelectableBinding

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
        val binding = ItemListSelectableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, selectedItems.contains(item))
        holder.itemView.setOnClickListener {
            toggleSelection(item)
            notifyItemChanged(position)
        }
    }

    class ViewHolder(private val binding: ItemListSelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, isSelected: Boolean) {
            binding.chipItem.text = item
            binding.chipItem.isChecked = isSelected
        }
    }
}