package com.example.storeit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.storeit.model.LocationDatabase
import com.example.storeit.model.TreeDatabase
import com.example.storeit.model.UIModel

val TREE_PREFERENCES = "TREE_PREFERENCES"
class MainActivity : AppCompatActivity() {
    var treeDatabase: LocationDatabase?  = null
    var uiModel: UIModel? = null

    val titleView: EditText?
        get() = supportActionBar?.customView?.findViewById(R.id.edit_title_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        treeDatabase = ViewModelProvider(this).get(LocationDatabase::class.java)
        uiModel = ViewModelProvider(this).get(UIModel::class.java)
//        treeDatabase?.loadTrees(TreeDatabase.loadFromStorage(getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE)))
        treeDatabase?.loadFromStorage(getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE))
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowCustomEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        val layoutInflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val editTitleView = layoutInflater.inflate(R.layout.editable_title, null)
        val layoutParams = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        supportActionBar?.setCustomView(editTitleView, layoutParams)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.storeit_fragment_container, SearchFragment.newInstance())
            addToBackStack(null)
        }
        transaction.commit()
//        supportFragmentManager.backStackEntryCount.let{
//            for (i in 0..it){
//                supportFragmentManager.popBackStack()
//            }
//        }
//        val currentTreeId = uiModel?.currentTreeId
//        val currentItemId = uiModel?.currentItemId
//        if (currentTreeId != null && currentItemId != null){
//            displayItemStack(currentItemId, currentTreeId)
//        }
//        else {
//            displayTreeStack(currentTreeId)
//            displayItem(currentItemId)
//        }
    }
    fun setTreeTitle(title: String?){
        supportActionBar?.title = title
    }

    fun displayTreeStack(id: String?){
        supportFragmentManager.backStackEntryCount.let{
            for (i in 0..it){
                supportFragmentManager.popBackStack()
            }
        }
        if (id == null) return
        val treeIdStack = mutableListOf(id)
        var parentId = treeDatabase?.getTreeById(id)?.parentId
        while (parentId != null){
            treeIdStack.add(parentId)
            parentId = treeDatabase?.getTreeById(parentId)?.parentId
        }
        for (i in treeIdStack.indices.reversed()){
            displayTree(treeIdStack[i])
        }
    }

    fun displayTree(id: String?){
        if (id == null) return
        val transaction = supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.storeit_fragment_container, LocationFragment.newInstance(id))
            addToBackStack(id)
        }
        transaction.commit()
        uiModel?.currentItemId = null
        uiModel?.currentTreeId = id
    }

    fun displayItem(id: String?){
        if (id == null) return
        val transaction = supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.storeit_fragment_container, ItemFragment.newInstance(id))
            addToBackStack(id)
        }
        transaction.commit()
        uiModel?.currentItemId = id
    }

    fun displayItemStack(id: String?, locationId: String?){
        if (id == null || locationId == null) return
        displayTreeStack(locationId)
        displayItem(id)
    }
}