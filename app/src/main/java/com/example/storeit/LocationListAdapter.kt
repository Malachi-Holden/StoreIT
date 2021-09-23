package com.example.storeit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Tree

class LocationListAdapter(private val helloTrees: List<Tree<MainActivity.Location>?>,
                          val tempTrees: List<Tree<MainActivity.Location>?> = listOf(),
                          val editMode: Boolean = false,
                          val childAction: (Int) -> Unit,
                          val toggleDeleteChildAction: (Int, Boolean) -> Unit
                            ): RecyclerView.Adapter<LocationListAdapter.HelloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelloViewHolder {
        return HelloViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: HelloViewHolder, position: Int) {
        holder.onBind(
            editMode,
            (helloTrees+tempTrees)[position]?.data?.title ?: "",
            {childAction(position)},
            {deleteSelected ->
                toggleDeleteChildAction(position, deleteSelected)
            }
        )

    }

    override fun getItemCount() = helloTrees.size + tempTrees.size

    class HelloViewHolder(view: View): RecyclerView.ViewHolder(view){
        var goToLocationButton: Button = view.findViewById(R.id.go_to_location_button)
        var deleteLocationButton: Button = view.findViewById(R.id.remove_location_button)
        var cancelDeleteButton: Button = view.findViewById(R.id.cancel_remove_button)
        var deleteSelected = false

        fun onBind(inEditMode: Boolean, locationText: String, locationClickListener: ()->Unit, deleteToggleListener: (Boolean)->Unit){
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
}