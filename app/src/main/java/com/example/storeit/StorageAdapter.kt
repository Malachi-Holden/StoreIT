package com.example.storeit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Location
import com.example.storeit.model.Tree

open class StorageAdapter(val baseTitles: List<String?>,
                          val tempTitles: List<String?> = listOf(),
                          val editMode: Boolean = false,
                          val newItemMessage: String? = null,
                          val childAction: (Int) -> Unit,
                          val toggleDeleteChildAction: (Int, Boolean) -> Unit,
                          val addChildAction: () -> Unit
                            ): RecyclerView.Adapter<StorageAdapter.LocationViewHolder>() {

    val LOCATION_VIEW_TYPE = 1
    val ADD_LOCATION_VIEW_TYPE = 2

    override fun getItemViewType(position: Int): Int {
        if (position < baseTitles.size + tempTitles.size) return LOCATION_VIEW_TYPE
        return ADD_LOCATION_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        if (viewType == ADD_LOCATION_VIEW_TYPE) {
            return NewLocationButtonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.new_location_item, parent, false))
        }
        return LocationItemViewHolder(LayoutInflater.from(parent.context).inflate( R.layout.location_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        if (holder.itemViewType == ADD_LOCATION_VIEW_TYPE && holder is NewLocationButtonViewHolder){
            holder.onBind(newItemMessage) { addChildAction() }
            return
        }
        if (holder.itemViewType != LOCATION_VIEW_TYPE || holder !is LocationItemViewHolder) return
        holder.onBind(
            editMode,
            (baseTitles+tempTitles)[position] ?: "",
            {childAction(position)},
            {deleteSelected ->
                toggleDeleteChildAction(position, deleteSelected)
            }
        )
    }

    override fun getItemCount() = baseTitles.size + tempTitles.size + editMode.compareTo(false)

    open class LocationViewHolder(view: View): RecyclerView.ViewHolder(view)

    open class LocationItemViewHolder(view: View): LocationViewHolder(view){
        var goToLocationButton: Button = view.findViewById(R.id.go_to_location_button)
        var deleteLocationButton: Button = view.findViewById(R.id.remove_location_button)
        var cancelDeleteButton: Button = view.findViewById(R.id.cancel_remove_button)
        var deleteSelected = false

        open fun onBind(inEditMode: Boolean, locationText: String, locationClickListener: ()->Unit, deleteToggleListener: (Boolean)->Unit){
            goToLocationButton.text = locationText
            goToLocationButton.setOnClickListener { locationClickListener() }
            goToLocationButton.isEnabled = !inEditMode

            deleteLocationButton.visibility = if (!inEditMode || deleteSelected) View.GONE else View.VISIBLE
            deleteLocationButton.setOnClickListener {
                deleteSelected = true
                deleteLocationButton.visibility = View.GONE
                cancelDeleteButton.visibility = View.VISIBLE
                deleteToggleListener(true)
            }

            cancelDeleteButton.visibility = if (!inEditMode || !deleteSelected) View.GONE else View.VISIBLE
            cancelDeleteButton.setOnClickListener {
                deleteSelected = false
                deleteLocationButton.visibility = View.VISIBLE
                cancelDeleteButton.visibility = View.GONE
                deleteToggleListener(false)
            }
        }
    }

    class NewLocationButtonViewHolder(view: View): LocationViewHolder(view){
        var newLocationButton: Button = view.findViewById(R.id.new_location_button)
        fun onBind(newLocationMessage: String? = null, onClick: ()->Unit ){
            newLocationButton.setOnClickListener { onClick() }
            if (newLocationMessage != null) newLocationButton.text = newLocationMessage
        }
    }
}