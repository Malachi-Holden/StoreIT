package com.example.storeit

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.storeit.model.UIModel
import com.example.storeit.model.ROOT_ID
import com.example.storeit.model.Tree
import com.example.storeit.model.TreeDatabase

val TREE_PREFERENCES = "TREE_PREFERENCES"
class MainActivity : AppCompatActivity() {
    var treeDatabase: LocationDatabase?  = null
    var uiModel: UIModel? = null
//    var titleTextChangedListener: ((text: String?)->Unit)? = null

    val titleView: EditText?
        get() = supportActionBar?.customView?.findViewById(R.id.edit_title_view)

//    var titleWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        treeDatabase = ViewModelProvider(this).get(LocationDatabase::class.java)
        uiModel = ViewModelProvider(this).get(UIModel::class.java)
        treeDatabase?.loadTrees(TreeDatabase.loadFromStorage(getSharedPreferences(TREE_PREFERENCES, Context.MODE_PRIVATE)))
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowCustomEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        val layoutInflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val editTitleView = layoutInflater.inflate(R.layout.editable_title, null)
        val layoutParams = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)
        supportActionBar?.setCustomView(editTitleView, layoutParams)
//        titleWatcher = object: TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            override fun afterTextChanged(s: Editable?) {
//                onTitleTextChanged(s.toString())
//            }
//        }
//        titleView?.addTextChangedListener(titleWatcher)
        supportFragmentManager.backStackEntryCount.let{
            for (i in 0..it){
                supportFragmentManager.popBackStack()
            }
        }
        uiModel?.currentTreeId?.let{
            displayTreeStack(it)
        }
    }

//    fun onTitleTextChanged(title: String){
//        titleTextChangedListener?.let { it(title) }
//    }

    fun setTreeTitle(title: String?){
//        titleView?.removeTextChangedListener(titleWatcher)
//        titleView?.setText(title)
//        titleView?.addTextChangedListener(titleWatcher)
        supportActionBar?.title = title
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