package com.example.storeit

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.storeit.model.Tree
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val ARG_TREE = "ARG_TREE"

class HelloListFragment : Fragment() {
    private var treeId: String? = null
    private var tree: Tree<MainActivity.Location>? = null
    private var helloList: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        treeId = arguments?.getString(ARG_TREE)
        val main = activity as MainActivity
        tree = main.treeDatabase?.getTreeById(treeId)
    }

    override fun onResume() {
        super.onResume()
        val main = activity as MainActivity
        main.supportActionBar?.title = tree?.data?.title

        main.supportActionBar?.setDisplayHomeAsUpEnabled(main.supportFragmentManager.backStackEntryCount > 1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = activity as MainActivity
        helloList = view.findViewById(R.id.hello_list)
        val adapterTrees = main.treeDatabase?.getChildrenById(treeId) ?: listOf()
        helloList?.adapter = LocationListAdapter(adapterTrees){ pos -> onChildClicked(pos) }
        helloList?.layoutManager = LinearLayoutManager(context)
        val newChildButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        newChildButton.setOnClickListener{onNewChildClicked()}
        val descriptionView: EditText = view.findViewById(R.id.description_view)
        descriptionView.setText(tree?.data?.description)
        descriptionView.addTextChangedListener {
            tree?.data?.description = it.toString()
            main.treeDatabase?.saveToStorage(main.getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE))
        }
    }

    private fun onChildClicked(pos: Int){
        tree?.children?.get(pos)?.let {
            (activity as MainActivity).displayTree(it)
        }
    }

    private fun onNewChildClicked(){
        val editText = EditText(context)
        editText.inputType = InputType.TYPE_CLASS_TEXT
        android.app.AlertDialog.Builder(context).setTitle("New Child")
            .setMessage("Enter Child Name")
            .setView(editText)
            .setNegativeButton("Cancel"){ _, _ -> }
            .setPositiveButton("Create"){ _, _ -> onNewChildCreated(editText.text.toString())}
            .show()
    }

    private fun onNewChildCreated(title: String){
        val main = activity as MainActivity
        main.treeDatabase?.addChild(Tree(data = MainActivity.Location(title)), treeId)
        main.treeDatabase?.let{
            helloList?.adapter = LocationListAdapter(it.getChildrenById(tree?.id)){ pos -> onChildClicked(pos) }
        }
        main.treeDatabase?.saveToStorage(main.getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_list, container, false)
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