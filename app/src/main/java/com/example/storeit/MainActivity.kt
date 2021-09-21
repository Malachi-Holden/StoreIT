package com.example.storeit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.example.storeit.model.UIModel
import com.example.storeit.model.ROOT_ID
import com.example.storeit.model.Tree
import com.example.storeit.model.TreeDatabase

val TREE_PREFERENCES = "TREE_PREFERENCES"
class MainActivity : AppCompatActivity() {
    var treeDatabase: LocationDatabase?  = null
    var uiModel: UIModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        treeDatabase = ViewModelProvider(this).get(LocationDatabase::class.java)
        uiModel = ViewModelProvider(this).get(UIModel::class.java)
        treeDatabase?.loadTrees(TreeDatabase.loadFromStorage(getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE)))
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportFragmentManager.backStackEntryCount.let{
            for (i in 0..it){
                supportFragmentManager.popBackStack()
            }
        }

        uiModel?.currentTreeId?.let{
            displayTreeStack(it)
        }
    }

    private fun displayTreeStack(id: String){
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

    fun displayTree(id: String){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.hello_fragment_container, HelloListFragment.newInstance(id))
            addToBackStack(id)
        }
        transaction.commit()
        uiModel?.currentTreeId = id
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class LocationDatabase: TreeDatabase<Location>(){
        var root: Tree<Location>? = null
        override fun root(): Tree<Location>{
            val localRoot = root ?: Tree(data = Location("home", "this is our home"), id = ROOT_ID)
            root = localRoot
            return localRoot
        }
    }

    class Location(var title: String, var description: String? = null)
}