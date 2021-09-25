package com.example.storeit

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Tree
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.inputmethod.InputMethodManager


private const val ARG_TREE = "ARG_TREE"

class HelloListFragment : Fragment() {
    private var treeId: String? = null
    private var tree: Tree<MainActivity.Location>? = null
    private var helloList: RecyclerView? = null

    var preSaveLocationList = mutableListOf<Tree<MainActivity.Location>?>()
    val locationsToDelete = mutableListOf<Int>()
    var beginEditButton: FloatingActionButton? = null
    var saveButton: FloatingActionButton? = null
    var editDescriptionView: EditText? = null
    var descriptionView: TextView? = null

    var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        treeId = arguments?.getString(ARG_TREE)
        val main = activity as MainActivity
        tree = main.treeDatabase?.getTreeById(treeId)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        val main = activity as MainActivity
        main.setTreeTitle(tree?.data?.title)
        main.supportActionBar?.setDisplayHomeAsUpEnabled(main.supportFragmentManager.backStackEntryCount > 1)
        preSaveLocationList.clear()
        reloadList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        helloList = view.findViewById(R.id.hello_list)
        preSaveLocationList.clear()
        reloadList()
        helloList?.layoutManager = LinearLayoutManager(context)
        beginEditButton = view.findViewById(R.id.edit_location_button)
        beginEditButton?.setOnClickListener{onEditClicked()}

        saveButton = view.findViewById(R.id.save_button)
        saveButton?.setOnClickListener { onSaveClicked() }

        editDescriptionView = view.findViewById(R.id.edit_description_view)

        descriptionView = view.findViewById(R.id.descriptionView)
        descriptionView?.text = tree?.data?.description
    }

    private fun reloadList() {
        val main = activity as MainActivity
        val adapterTrees = main.treeDatabase?.getChildrenById(treeId) ?: listOf()
        helloList?.adapter = LocationListAdapter(adapterTrees,
            preSaveLocationList,
            isEditing,
            { pos -> onChildClicked(pos) },
            { pos, deleteSelected -> onDeleteChildToggled(pos, deleteSelected)},
            { onNewChildClicked() }
        )
    }

    private fun onChildClicked(pos: Int){
        tree?.children?.get(pos)?.let {
            (activity as MainActivity).displayTree(it)
        }
    }

    private fun onNewChildClicked(){
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(context).setTitle("New Location")
            .setMessage("Enter Location Name")
            .setView(editText)
            .setNegativeButton("Cancel"){ _, _ -> }
            .setPositiveButton("Create"){ _, _ -> onNewChildCreated(editText.text.toString())}
            .show()
    }

    private fun onDeleteChildToggled(pos: Int, deleteSelected: Boolean){
        if (deleteSelected){
            locationsToDelete.add(pos)
        }
        else{
            locationsToDelete.remove(pos)
        }
    }

    private fun deleteChild(pos: Int){
        val treeSize = tree?.children?.size ?: 0
        if (pos >= treeSize){
            preSaveLocationList.removeAt(pos - treeSize)
            return
        }
        val main = activity as MainActivity
        val childId = main.treeDatabase?.getChildrenById(treeId)?.get(pos)?.id
        main.treeDatabase?.recursiveDeleteTreeById(childId)
    }

    private fun onEditClicked(){
        beginEditButton?.visibility = View.INVISIBLE
        saveButton?.visibility = View.VISIBLE
        val main = activity as MainActivity
        main.supportActionBar?.setDisplayShowCustomEnabled(true)
        main.supportActionBar?.setDisplayShowTitleEnabled(false)
        main.titleView?.setText(tree?.data?.title)
        editDescriptionView?.setText(tree?.data?.description)
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
            .setMessage("Anything you deleted cannot be restored.")
            .setNegativeButton("Cancel"){_,_ ->}
            .setPositiveButton("Continue"){_,_ ->
                save()
                endEditing()
            }
            .show()
    }

    private fun save(){
        val main = activity as MainActivity
        val newDescription = editDescriptionView?.text.toString()
        val data = tree?.data ?: MainActivity.Location("")
        data.description = newDescription
        data.title = main.titleView?.text?.toString() ?: ""
        tree?.data = data
        for (childPosition in locationsToDelete){
            deleteChild(childPosition)
        }
        for (child in preSaveLocationList){
            if (child != null) main.treeDatabase?.addChild(child, treeId)
        }
        locationsToDelete.clear()
        preSaveLocationList.clear()
        main.treeDatabase?.saveToStorage(main.getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE))
    }

    private fun endEditing(){
        beginEditButton?.visibility = View.VISIBLE
        saveButton?.visibility = View.INVISIBLE
        val main = activity as MainActivity
        main.supportActionBar?.setDisplayShowCustomEnabled(false)
        main.supportActionBar?.setDisplayShowTitleEnabled(true)
        main.supportActionBar?.title = tree?.data?.title
        editDescriptionView?.visibility = View.INVISIBLE
        descriptionView?.visibility = View.VISIBLE
        descriptionView?.text = tree?.data?.description
        main.supportActionBar?.setHomeAsUpIndicator(null)
        main.supportActionBar?.setDisplayHomeAsUpEnabled(main.supportFragmentManager.backStackEntryCount > 1)
        isEditing = false
        reloadList()
        hideSoftKeyboard()
    }

    private fun onNewChildCreated(title: String){
        preSaveLocationList.add(Tree(data = MainActivity.Location(title)))
        reloadList()
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

    companion object {
        @JvmStatic
        fun newInstance(argTreeId: String) =
            HelloListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TREE, argTreeId)
                }
            }
    }
}