package com.example.storeit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Tree

class LocationListAdapter(private val helloTrees: List<Tree<MainActivity.Location>?>, val childAction: (Int) -> Unit): RecyclerView.Adapter<LocationListAdapter.HelloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelloViewHolder {
        return HelloViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.location_row_item, parent, false))
    }

    override fun onBindViewHolder(holder: HelloViewHolder, position: Int) {
        holder.helloButton.text = helloTrees[position]?.data?.title
        holder.helloButton.setOnClickListener { childAction(position) }
    }

    override fun getItemCount() = helloTrees.size

    class HelloViewHolder(view: View): RecyclerView.ViewHolder(view){
        var helloButton: Button = view.findViewById(R.id.hello_button)
    }
}