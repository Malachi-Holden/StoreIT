package com.example.storeit

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.adapter.SearchAdapter
import com.example.storeit.model.Item
import com.example.storeit.model.Location
import com.example.storeit.model.Tree

val SEARCH_DIALOG_SIZE_PERCENTAGE = 0.8
class SearchFragment : DialogFragment(), SearchView.OnQueryTextListener {
    var searchBar: SearchView? = null
    var searchResultsView: RecyclerView? = null

    var locationResults: List<Tree<Location>> = listOf()
    var itemResults: List<Item> = listOf()
    var cancelButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Dialog)
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
        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton?.setOnClickListener { dismiss() }
        reloadSearch("")
//        view.post {
//            val dialogWindow = dialog?.window
//            dialogWindow?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
//            dialogWindow?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//            view.invalidate()
//        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        val size = Point()
        val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            activity?.display
        } else {
            @Suppress("DEPRECATION")
            activity?.windowManager?.defaultDisplay
        }
        display?.getRealSize(size)
        window?.setLayout((size.x * SEARCH_DIALOG_SIZE_PERCENTAGE).toInt(), (size.y * SEARCH_DIALOG_SIZE_PERCENTAGE).toInt())
        window?.setGravity(Gravity.CENTER)
        showsDialog = true
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    fun reloadSearch(searchText: String){
        val main = activity as MainActivity
        locationResults = main.treeDatabase?.searchLocations(searchText) ?: listOf()
        itemResults = main.treeDatabase?.searchItems(searchText) ?: listOf()

        val locationTitles = locationResults.map { it.data?.title ?: "" }
        val itemTitles = itemResults.map { it.title }

        searchResultsView?.adapter = SearchAdapter(
            locationTitles,
            { onSearchLocationClicked(it) },
            itemTitles,
            { onSearchItemClicked(it) }
        )
    }

    fun onSearchLocationClicked(position: Int){
        dismiss()
        (activity as MainActivity).displayTreeStack(locationResults[position].id)
    }

    fun onSearchItemClicked(position: Int){
        dismiss()
        (activity as MainActivity).displayItem(itemResults[position].id)
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