package com.example.storeit.model

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject


class Location(var title: String, var description: String? = null, var items: HashMap<String, Int>? = null)

val LOCATION_STORAGE_KEY = "LOCATION_STORAGE_KEY"
val ITEM_STORAGE_KEY = "ITEM_STORAGE_KEY"
class LocationDatabase: TreeDatabase<Location>(){
    var root: Tree<Location>? = null
    override fun root(): Tree<Location>{
        val localRoot = root ?: Tree(data = Location("home", "this is our home"), id = ROOT_ID)
        root = localRoot
        return localRoot
    }

    var highestItemId = 1
    var items = hashMapOf<String, Item>()

    fun addItem(item: Item){
        highestItemId ++
        val itemid = highestItemId.toString()
        item.id = itemid
        items[itemid] = item
    }

    fun storeItemAtLocation(itemId: String, locationId: String, count: Int = 1){
        items[itemId]?.locations?.add(locationId)
        val location = getTreeById(locationId) ?: return
        val items = location.data?.items ?: hashMapOf()
        val previousCopies = items[itemId] ?: 0
        items[itemId] = previousCopies + count
        location.data?.items = items
    }

    fun removeItemFromLocation(itemId: String, locationId: String){
        items[itemId]?.locations?.remove(locationId)
        val location = getTreeById(locationId) ?: return
        location.data?.items?.remove(itemId)
    }


    fun getLocationsForItem(id: String?) = items[id]?.locations?.map {
        getTreeById(it)
    } ?: listOf<Tree<Location>>()

    override fun toString(): String {
        val treeJson = JSONObject()
        treeJson.put(TREE_STORAGE_KEY, super.toString())
        val gson = Gson()
        val itemString = gson.toJson(items)
        treeJson.put(ITEM_STORAGE_KEY, itemString)
        return treeJson.toString()
    }

    override fun saveToStorage(preferences: SharedPreferences){
        val editor = preferences.edit()
        editor.putString(LOCATION_STORAGE_KEY, toString())
        editor.apply()
    }

    fun loadFromStorage(preferences: SharedPreferences){
        val jsonString = preferences.getString(LOCATION_STORAGE_KEY, "{}") ?: "{}"
        val jsonData = JSONObject(jsonString)
        val itemString = try{jsonData.getString(ITEM_STORAGE_KEY)} catch (e: JSONException){return}
        val treeString = try{jsonData.getString(TREE_STORAGE_KEY)} catch (e: JSONException){return}

        val gson = Gson()
        val itemMapType = object : TypeToken<HashMap<String, Item>>(){}.type
        val treeMapType = object : TypeToken<HashMap<String, Tree<Location>>>(){}.type
        val rawItems = gson.fromJson<HashMap<String, Item>>(itemString, itemMapType)
        val rawTrees = gson.fromJson<HashMap<String, Tree<Location>>>(treeString, treeMapType)
        loadTrees(rawTrees)
        loadItems(rawItems)
    }

    fun loadItems(itemMap: HashMap<String, Item>){
        items.clear()
        items.putAll(itemMap)
        for ((id, _) in items){
            try {
                val idNum = id.toInt()
                if (idNum > highestItemId){
                    highestItemId = idNum
                }
            }catch (e: NumberFormatException){
                continue
            }
        }
    }

    fun searchLocations(searchText: String): List<Tree<Location>>{
        if (searchText.isEmpty()) return listOf()
        return trees.values.filter {
            it.data?.title?.contains(searchText) ?: false
        }
    }

    fun searchItems(searchText: String): List<Item>{
        if (searchText.isEmpty()) return listOf()
        return items.values.filter {
            it.title.contains(searchText)
        }
    }
}