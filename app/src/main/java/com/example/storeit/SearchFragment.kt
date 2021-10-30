package com.example.storeit

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.adapter.SearchAdapter

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    var searchBar: SearchView? = null
    var searchResultsView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchBar = view.findViewById(R.id.search_view)
        searchResultsView = view.findViewById(R.id.search_results_view)
        searchResultsView?.layoutManager = LinearLayoutManager(context)

        val searchManager= activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchBar?.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchBar?.setOnQueryTextListener(this)
        searchBar?.setOnClickListener {
            searchBar?.isIconified = false
        }
        reloadSearch("")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    fun reloadSearch(searchText: String){
        val main = activity as MainActivity
        val locations = main.treeDatabase?.searchLocations(searchText) ?: listOf()
        val items = main.treeDatabase?.searchItems(searchText) ?: listOf()

        val locationTitles = locations.map { it.data?.title ?: "" }
        val itemTitles = items.map { it.title }

        searchResultsView?.adapter = SearchAdapter(
            locationTitles,
            { onSearchLocationClicked(it) },
            itemTitles,
            { onSearchItemClicked(it) }
        )
    }

    fun onSearchLocationClicked(position: Int){

    }

    fun onSearchItemClicked(position: Int){

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        reloadSearch(newText ?: "")
        return true    }
}