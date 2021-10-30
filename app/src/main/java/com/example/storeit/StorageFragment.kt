package com.example.storeit

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.inputmethod.InputMethodManager
import com.example.storeit.adapter.StorageAdapter
import com.example.storeit.model.Location
import com.example.storeit.model.Tree


const val ARG_STORAGE = "ARG_STORAGE"
abstract class StorageFragment : Fragment() {
    var contentId: String? = null
    var locationsView: RecyclerView? = null

    var beginEditButton: FloatingActionButton? = null
    var saveButton: FloatingActionButton? = null
    var editDescriptionView: EditText? = null
    var descriptionView: TextView? = null

    var preSaveLocationList = mutableListOf<Tree<Location>?>()
    val locationsToDelete = mutableListOf<Int>()

    var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentId = arguments?.getString(ARG_STORAGE)
//        treeId = arguments?.getString(ARG_TREE)
//        val main = activity as MainActivity
//        tree = main.treeDatabase?.getTreeById(treeId)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        val main = activity as MainActivity
        main.setTreeTitle(storageTitle())
        main.supportActionBar?.setDisplayHomeAsUpEnabled(main.supportFragmentManager.backStackEntryCount > 1)
        preSaveLocationList.clear()
        reloadList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationsView = view.findViewById(R.id.location_list_view)
        preSaveLocationList.clear()
        reloadList()
        locationsView?.layoutManager = LinearLayoutManager(context)
        beginEditButton = view.findViewById(R.id.edit_location_button)
        beginEditButton?.setOnClickListener{onEditClicked()}

        saveButton = view.findViewById(R.id.save_button)
        saveButton?.setOnClickListener { onSaveClicked() }

        editDescriptionView = view.findViewById(R.id.edit_description_view)

        descriptionView = view.findViewById(R.id.descriptionView)
        descriptionView?.text = storageDescription()
    }

    fun reloadList() {
        val main = activity as MainActivity
        val adapterTrees = locations().map { main.treeDatabase?.getTreeById(it)?.data?.title }
        val tempAdapterTrees = preSaveLocationList.map { it?.data?.title }
        locationsView?.adapter = StorageAdapter(
            adapterTrees,
            tempAdapterTrees,
            isEditing,
            "New Location",
            { pos -> onChildClicked(pos) },
            { pos, deleteSelected -> onDeleteChildToggled(pos, deleteSelected)},
            { onNewChildClicked() }
        )
    }

    abstract fun onChildClicked(pos: Int)

    abstract fun onNewChildClicked()

    private fun onDeleteChildToggled(pos: Int, deleteSelected: Boolean){
        if (deleteSelected){
            locationsToDelete.add(pos)
        }
        else{
            locationsToDelete.remove(pos)
        }
    }

    open fun onEditClicked(){
        beginEditButton?.visibility = View.INVISIBLE
        saveButton?.visibility = View.VISIBLE
        val main = activity as MainActivity
        main.supportActionBar?.setDisplayShowCustomEnabled(true)
        main.supportActionBar?.setDisplayShowTitleEnabled(false)
        main.titleView?.setText(storageTitle())
        editDescriptionView?.setText(storageDescription())
        editDescriptionView?.visibility = View.VISIBLE
        descriptionView?.visibility = View.INVISIBLE
        main.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        main.supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel)
        isEditing = true
        preSaveLocationList.clear()
        reloadList()
    }

    private fun onEndEditClicked(){
        endEditing()
    }

    private fun onSaveClicked(){
        AlertDialog.Builder(context)
            .setTitle("Save changes?")
            .setMessage("Anything you deleted cannot be restored.") // make this customizable
            .setNegativeButton("Cancel"){_,_ ->}
            .setPositiveButton("Continue"){_,_ ->
                val main = activity as MainActivity
                save()
                main.treeDatabase?.saveToStorage(main.getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE))
                endEditing()
            }
            .show()
    }

    abstract fun save()

    open fun endEditing(){
        beginEditButton?.visibility = View.VISIBLE
        saveButton?.visibility = View.INVISIBLE
        val main = activity as MainActivity
        main.supportActionBar?.setDisplayShowCustomEnabled(false)
        main.supportActionBar?.setDisplayShowTitleEnabled(true)
        main.supportActionBar?.title = storageTitle()
        editDescriptionView?.visibility = View.INVISIBLE
        descriptionView?.visibility = View.VISIBLE
        descriptionView?.text = storageDescription()
        main.supportActionBar?.setHomeAsUpIndicator(null)
        main.supportActionBar?.setDisplayHomeAsUpEnabled(main.supportFragmentManager.backStackEntryCount > 1)
        isEditing = false
        reloadList()
        hideSoftKeyboard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val main = activity as MainActivity
        if (item.itemId == android.R.id.home){
            if (isEditing){
                onEndEditClicked()
                return true
            }
            main.supportFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_list, container, false)
    }

    fun hideSoftKeyboard() {
        val main = activity as MainActivity
        val imm = main.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)

    }

    abstract fun storageTitle(): String
    abstract fun storageDescription(): String
    abstract fun locations(): List<String?>
}