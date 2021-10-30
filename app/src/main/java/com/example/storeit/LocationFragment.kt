package com.example.storeit

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.adapter.ItemAdapter
import com.example.storeit.model.Item
import com.example.storeit.model.Location
import com.example.storeit.model.Tree

private const val ARG_TREE = "ARG_TREE"

class LocationFragment: StorageFragment() {
    var tree: Tree<Location>? = null
    var itemListview: RecyclerView? = null
    var itemIdList = mutableListOf<String>()

    var preSaveItemList = mutableListOf<Pair<Item?, Int?>>()
    val itemsToDelete = mutableListOf<Int>()

    override fun storageTitle() = tree?.data?.title ?: ""
    override fun storageDescription() = tree?.data?.description ?: ""

    override fun locations() = tree?.children ?: listOf()


    override fun onChildClicked(pos: Int) {
        tree?.children?.get(pos)?.let {
            (activity as MainActivity).displayTree(it)
        }
    }

    override fun save() {
        val main = activity as MainActivity
        val newDescription = editDescriptionView?.text.toString()
        val data = tree?.data ?: Location("")
        data.description = newDescription
        data.title = main.titleView?.text?.toString() ?: ""
        tree?.data = data
        for (childPosition in locationsToDelete){
            deleteChild(childPosition)
        }
        for (child in preSaveLocationList){
            if (child != null) main.treeDatabase?.addChild(child, contentId)
        }
        locationsToDelete.clear()
        preSaveLocationList.clear()

        for (itemPosition in itemsToDelete){
            deleteItem(itemPosition)
        }
        for ((item, count) in preSaveItemList){
            if (item == null) continue
            main.treeDatabase?.addItem(item)
            val itemId = item.id ?: continue
            val treeId = contentId ?: continue
            main.treeDatabase?.storeItemAtLocation(itemId, treeId, count ?: 1)
        }
        itemsToDelete.clear()
        preSaveItemList.clear()
    }


    override fun endEditing() {
        super.endEditing()
        reloadItems()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val main = activity as MainActivity
        tree = main.treeDatabase?.getTreeById(contentId)
    }


    override fun onNewChildClicked(){
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(context).setTitle("New Location")
            .setMessage("Enter Location Name")
            .setView(editText)
            .setNegativeButton("Cancel"){ _, _ -> }
            .setPositiveButton("Create"){ _, _ -> onNewChildCreated(editText.text.toString())}
            .show()
    }

    private fun deleteChild(pos: Int){
        val treeSize = tree?.children?.size ?: 0
        if (pos >= treeSize){
            preSaveLocationList.removeAt(pos - treeSize)
            return
        }
        val main = activity as MainActivity
        val childId = main.treeDatabase?.getChildrenById(contentId)?.get(pos)?.id
        main.treeDatabase?.recursiveDeleteTreeById(childId)
    }

    private fun onNewChildCreated(title: String){
        preSaveLocationList.add(Tree(data = Location(title)))
        reloadList()
    }

    override fun onResume() {
        super.onResume()
        preSaveLocationList.clear()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemListview = view.findViewById(R.id.item_list_view)
        itemListview?.layoutManager = LinearLayoutManager(context)
        itemListview?.visibility = View.VISIBLE
        reloadItems()
        preSaveLocationList.clear()
    }

    override fun onEditClicked() {
        super.onEditClicked()
        reloadItems()
    }

    fun reloadItems(){
        val main = activity as MainActivity
        itemIdList.clear()
        val baseTitles = mutableListOf<String>()
        val itemCounts = mutableListOf<Int>()
        for ((id, count) in tree?.data?.items ?: hashMapOf()){
            itemIdList.add(id)
            itemCounts.add(count)
            baseTitles.add("${main.treeDatabase?.items?.get(id)?.title ?: ""}: $count")
        }
        val tempItemTitles = preSaveItemList.map { (item, count) ->
            itemCounts.add(count ?: 1)
            "${item?.title ?: ""}: $count"
        }
        itemListview?.adapter = ItemAdapter(
            baseTitles,
            tempItemTitles,
            itemCounts,
            isEditing,
            "New Item",
            { pos -> onItemClicked(pos) },
            { pos, deleteSelected -> onDeleteItemToggled(pos, deleteSelected)},
            { onNewItemClicked() },
            { diff, pos -> onItemCountChanged(diff, pos) }
        )
    }

    fun onItemCountChanged(diff: Int, pos: Int){
        val itemListSize = itemIdList.size
        if (pos > itemListSize){
            preSaveItemList[pos]
            return
        }

        val itemId = itemIdList[pos]
        val treeItems = tree?.data?.items ?: return
        treeItems[itemId] = (treeItems[itemId] ?: 0) + diff
        reloadItems()
    }

    fun deleteItem(pos: Int){
        val itemListSize = itemIdList.size
        if (pos > itemListSize){
            preSaveItemList.removeAt(pos - itemListSize)
            return
        }
        val main = activity as MainActivity
        val itemId = itemIdList[pos]
        main.treeDatabase?.removeItemFromLocation(itemId, contentId ?: return)
    }
    fun onItemClicked(pos: Int){
        itemIdList[pos].let {
            (activity as MainActivity).displayItem(it)
        }
    }

    fun onDeleteItemToggled(pos: Int, deleteSelected: Boolean){
        if (deleteSelected){
            itemsToDelete.add(pos)
        }
        else{
            itemsToDelete.remove(pos)
        }
    }

    fun onNewItemClicked(){
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(context).setTitle("New Item")
            .setMessage("Enter Item Name")
            .setView(editText)
            .setNegativeButton("Cancel"){ _, _ -> }
            .setPositiveButton("Create"){ _, _ -> onNewItemCreated(editText.text.toString())}
            .show()
    }

    fun onNewItemCreated(title: String){
        preSaveItemList.add(Pair(Item(title = title), 1))
        reloadItems()
    }

    companion object {
        @JvmStatic
        fun newInstance(argTreeId: String) =
            LocationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STORAGE, argTreeId)
                }
            }
    }
}