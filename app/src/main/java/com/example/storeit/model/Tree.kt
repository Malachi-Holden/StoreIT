package com.example.storeit.model

class Tree<T>(var id: String? = null, var parentId: String? = null, var children: MutableList<String> = mutableListOf(), var data: T? = null){
    override fun equals(other: Any?): Boolean {
        if (other is Tree<*>){
            return other.id == id && other.children == children && other.data == data
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + children.hashCode()
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }
}