package com.example.storeit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Tree

class LocationListAdapter(private val helloTrees: List<Tree<MainActivity.Location>?>,
                          val tempTrees: List<Tree<MainActivity.Location>?> = listOf(),
                          val editMode: Boolean = false,
                          val childAction: (Int) -> Unit,
                          val deleteChildAction: (Int) -> Unit
                            ): RecyclerView.Adapter<LocationListAdapter.HelloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelloViewHolder {
        return HelloViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: HelloViewHolder, position: Int) {
        holder.goToLocationButton.text = (helloTrees+tempTrees)[position]?.data?.title
        holder.goToLocationButton.setOnClickListener { childAction(position) }
        holder.goToLocationButton.isEnabled = !editMode

        holder.deleteLocationButton.setOnClickListener { deleteChildAction(position) }
        holder.deleteLocationButton.visibility = if (editMode) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = helloTrees.size + tempTrees.size

    class HelloViewHolder(view: View): RecyclerView.ViewHolder(view){
        var goToLocationButton: Button = view.findViewById(R.id.go_to_location_button)
        var deleteLocationButton: Button = view.findViewById(R.id.remove_location_button)
    }
}