package com.example.storeit

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import com.example.storeit.model.Item

class ItemFragment: StorageFragment() {
    var item: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val main = activity as MainActivity
        item = main.treeDatabase?.items?.get(contentId)
    }

    override fun onChildClicked(pos: Int) {
        val main = activity as MainActivity
        item?.locations?.get(pos)?.let {
            main.displayTreeStack(it)
        }
    }

    override fun onNewChildClicked(){
        // search for location
    }

    override fun save() {
        val main = activity as MainActivity
        val newDescription = editDescriptionView?.text.toString()
        item?.description = newDescription
        item?.title = main.titleView?.text?.toString() ?: ""
        for (childPosition in locationsToDelete){
            deleteChild(childPosition)
        }
        for (child in preSaveLocationList){
            if (child == null) continue
            item?.locations?.add(child.id)
        }

        locationsToDelete.clear()
        preSaveLocationList.clear()
    }

    override fun storageTitle() = item?.title ?: ""

    override fun storageDescription() = item?.description ?: ""

    override fun locations() = item?.locations ?: listOf()

    fun deleteChild(pos: Int){
        item?.locations?.removeAt(pos)
    }

    companion object {
        @JvmStatic
        fun newInstance(argItemId: String) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STORAGE, argItemId)
                }
            }
    }
}