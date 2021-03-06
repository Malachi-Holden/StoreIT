package com.example.storeit.model

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.storeit.model.Tree
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val TREE_STORAGE_KEY = "TREE_STORAGE_KEY"
val ROOT_ID = "1"
abstract class TreeDatabase<T>: ViewModel() {
    open val trees = hashMapOf<String, Tree<T>>()

    var highestId = 1


    fun addTree(tree: Tree<T>){
        highestId ++
        tree.id = highestId.toString()
        trees[highestId.toString()] = tree
    }

    fun addChild(child: Tree<T>, parentId: String?){
        val parent = getTreeById(parentId) ?: return
        addTree(child)
        child.id?.let{
            parent.children.add(it)
        }
        child.parentId = parent.id
    }

    fun getTreeById(id: String?) = if (id == ROOT_ID) root() else trees[id]

    fun getChildrenById(id: String?) = getTreeById(id)?.
        children?.
            map {
                getTreeById(it)
            }



    fun recursiveDeleteTreeById(id: String?){
        val tree = getTreeById(id) ?: return
        recursiveDeleteChildren(tree)
        val parent = getTreeById(tree.parentId)
        trees.remove(id)
        parent?.children?.remove(id)
    }

    private fun recursiveDeleteChildren(tree: Tree<T>){
        for (childId in tree.children){
            val child = getTreeById(childId) ?: continue
            recursiveDeleteChildren(child)
            trees.remove(childId)
        }
        tree.children.clear()
    }

    override fun toString(): String {
        val allTrees = HashMap(trees)
        allTrees[ROOT_ID] = root()
        val gson = Gson()
        return gson.toJson(allTrees)
    }

    open fun saveToStorage(preferences: SharedPreferences){
        val editor = preferences.edit()
        val treeString = toString()
        editor.putString(TREE_STORAGE_KEY, treeString)
        editor.apply()
    }

    open fun loadTrees(treeMap: HashMap<String, Tree<T>>){
        treeMap[ROOT_ID]?.data?.let{
            root().data = it
        }

        trees.clear()
        root().children.clear()
        treeMap.remove(ROOT_ID)
        trees.putAll(treeMap)
        for ((id, tree) in trees){
            if (tree.parentId == ROOT_ID){
                root().children.add(id)
            }
            try {
                val idNum = id.toInt()
                if (idNum > highestId){
                    highestId = idNum
                }
            }catch (e: NumberFormatException){
                continue
            }
        }
    }

    abstract fun root(): Tree<T>

    companion object{
        inline fun <reified S> getObjectFromString(string: String, converter: Gson): HashMap<String, Tree<S>> {
            val treeMapType = object : TypeToken<HashMap<String, Tree<S>>>() {}.type
            return converter.fromJson(string, treeMapType)
        }

        inline fun <reified S>loadFromStorage(preferences: SharedPreferences):HashMap<String, Tree<S>>{
            val jsonString = preferences.getString(TREE_STORAGE_KEY, "{}")
            val gson = Gson()
            var result = hashMapOf<String, Tree<S>>()
            if (jsonString != null) {
                result = getObjectFromString(jsonString, gson)
            }
            return result
        }
    }
}