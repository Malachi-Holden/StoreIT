package com.example.storeit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.storeit.R

class ItemAdapter(baseTitles: List<String?>,
                  tempTitles: List<String?> = listOf(),
                  var itemCounts: List<Int>,
                  editMode: Boolean = false,
                  newItemMessage: String? = null,
                  childAction: (Int) -> Unit,
                  toggleDeleteChildAction: (Int, Boolean) -> Unit,
                  addChildAction: () -> Unit,
                  var itemCountChangedAction: (Int, Int)->Unit
    ) : StorageAdapter(baseTitles, tempTitles, editMode, newItemMessage, childAction, toggleDeleteChildAction, addChildAction){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        if (viewType == LOCATION_VIEW_TYPE){
            return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_row_item, parent, false))
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.itemViewType != LOCATION_VIEW_TYPE || holder !is ItemViewHolder) return
        holder.onBindItem(editMode, itemCounts[position]){ diff -> itemCountChangedAction(diff, position) }
    }

    class ItemViewHolder(view: View): LocationItemViewHolder(view){
        var decrementButton: Button = view.findViewById(R.id.decrement_count)
        var incrementButton: Button = view.findViewById(R.id.increment_count)

        fun onBindItem(
            inEditMode: Boolean,
            itemCount: Int,
            onItemCountChanged: (Int)->Unit
        ) {
            decrementButton.visibility = if (inEditMode) View.VISIBLE else View.GONE
            decrementButton.isEnabled = itemCount > 0
            incrementButton.visibility = if (inEditMode) View.VISIBLE else View.GONE
            incrementButton.setOnClickListener { onItemCountChanged(1) }
            decrementButton.setOnClickListener { onItemCountChanged(-1) }
        }
    }
}