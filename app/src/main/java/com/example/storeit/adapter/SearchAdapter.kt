package com.example.storeit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.R

val ITEM_TYPE_RESULT = 0
val ITEM_TYPE_HEADER = 1

class SearchAdapter(
        var locationTitles: List<String>,
        var onLocationClicked: (Int)->Unit,
        var itemTitles: List<String>,
        var onItemClicked: (Int)->Unit,
        ): RecyclerView.Adapter<SearchAdapter.SearchResultView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultView {
        if (viewType == ITEM_TYPE_HEADER){
            return SearchHeader(LayoutInflater.from(parent.context).inflate(R.layout.search_result_header, parent, false))
        }
        return SearchItem(LayoutInflater.from(parent.context).inflate(R.layout.location_row_item, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 || position == 1 + locationTitles.size) return ITEM_TYPE_HEADER
        return ITEM_TYPE_RESULT
    }

    override fun onBindViewHolder(holder: SearchResultView, position: Int) {
        if (position == 0){
            (holder as? SearchHeader)?.onBind("Location results")
            return
        }
        if (position < 1 + locationTitles.size){
            onBindLocation(holder as? SearchItem ?: return, position - 1)
            return
        }
        if (position == 1 + locationTitles.size){
            (holder as? SearchHeader)?.onBind("Item results")
            return
        }
        onBindItem(holder as? SearchItem ?: return, position - 1 - locationTitles.size - 1)
    }

    fun onBindLocation(holder: SearchItem, locationPosition: Int){
        holder.onBind(locationTitles[locationPosition]){ onLocationClicked(locationPosition) }
    }

    fun onBindItem(holder: SearchItem, itemPosition: Int){
        holder.onBind(itemTitles[itemPosition]){ onItemClicked(itemPosition) }
    }

    override fun getItemCount() = 1 + locationTitles.size + 1 + itemTitles.size

    open class SearchResultView(view: View): RecyclerView.ViewHolder(view)

    class SearchItem(view: View): SearchResultView(view){
        var searchResultButton: Button = view.findViewById(R.id.go_to_location_button)
        var cancelButton: Button = view.findViewById(R.id.cancel_remove_button)
        fun onBind(title: String, onResultTapped: ()->Unit){
            cancelButton.visibility = View.GONE
            searchResultButton.setOnClickListener { onResultTapped() }
            searchResultButton.text = title
        }
    }

    class SearchHeader(view: View): SearchResultView(view){
        var titleView: TextView = view.findViewById(R.id.header_title)
        fun onBind(title: String){
            titleView.text = title
            titleView.visibility = View.VISIBLE
        }
    }
}