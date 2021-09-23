package com.example.storeit.model

import android.content.SharedPreferences
import junit.framework.TestCase
import org.junit.Before
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class TreeDatabaseTest : TestCase() {

    class TestTreeDatabaseImp: TreeDatabase<String>(){
        var root: Tree<String>? = null
        override fun root(): Tree<String>{
            val localRoot = root ?: Tree(data = "root", id = ROOT_ID)
            root = localRoot
            return localRoot
        }
    }

    var testDatabase = TestTreeDatabaseImp()

    @Before
    override fun setUp() {
        super.setUp()
        testDatabase = TestTreeDatabaseImp()
    }

    fun testAddTree() {
        testDatabase.addTree(Tree(data="2"))
        assertEquals(testDatabase.trees["2"], Tree(data="2", id="2"))
        testDatabase.addTree(Tree(data="3"))
        assertEquals(testDatabase.trees["3"], Tree(data="3", id="3"))
        testDatabase.addTree(Tree(data="4"))
        assertEquals(testDatabase.trees["4"], Tree(data="4", id="4"))
        testDatabase.addTree(Tree(data="5"))
        assertEquals(testDatabase.trees["5"], Tree(data="5", id="5"))
        testDatabase.addTree(Tree(data="6"))
        assertEquals(testDatabase.trees["6"], Tree(data="6", id="6"))
    }

    fun testAddChild() {val a = Tree(data="a")
        testDatabase.addChild(a, "1")
        val aa = Tree(data="aa")
        testDatabase.addChild(aa, a.id)
        val ab = Tree(data="ab")
        testDatabase.addChild(ab, a.id)
        val ac = Tree(data="ac")
        testDatabase.addChild(ac, a.id)
        val aaa = Tree(data="aaa")
        testDatabase.addChild(aaa, aa.id)

        assertEquals(testDatabase.root?.children?.get(0), a.id)
        assertEquals(testDatabase.trees[a.id], a)
        assertEquals(testDatabase.trees[a.id]?.parentId, "1")

        assertEquals(testDatabase.trees[a.id]?.children?.get(0), aa.id)
        assertEquals(testDatabase.trees[aa.id], aa)
        assertEquals(testDatabase.trees[aa.id]?.parentId, a.id)

        assertEquals(testDatabase.trees[a.id]?.children?.get(1), ab.id)
        assertEquals(testDatabase.trees[ab.id], ab)
        assertEquals(testDatabase.trees[ab.id]?.parentId, a.id)

        assertEquals(testDatabase.trees[a.id]?.children?.get(2), ac.id)
        assertEquals(testDatabase.trees[ac.id], ac)
        assertEquals(testDatabase.trees[ac.id]?.parentId, a.id)

        assertEquals(testDatabase.trees[aa.id]?.children?.get(0), aaa.id)
        assertEquals(testDatabase.trees[aaa.id], aaa)
        assertEquals(testDatabase.trees[aaa.id]?.parentId, aa.id)
    }

    fun testRecursiveDeleteTreeById(){
        val a = Tree(data="a")
        testDatabase.addChild(a, "1")
        val aa = Tree(data="aa")
        testDatabase.addChild(aa, a.id)
        val ab = Tree(data="ab")
        testDatabase.addChild(ab, a.id)
        val ac = Tree(data="ac")
        testDatabase.addChild(ac, a.id)
        val aaa = Tree(data="aaa")
        testDatabase.addChild(aaa, aa.id)
        testDatabase.recursiveDeleteTreeById(a.id)
        assertEquals(testDatabase.trees.size, 0)
        assertEquals(testDatabase.root?.children?.size, 0)
    }

    fun testGetTreeById() {
        testDatabase.addTree(Tree(data="hello world"))
        assertEquals(testDatabase.getTreeById("1"), Tree(data = "root", id = "1"))
        assertEquals(testDatabase.getTreeById("2"), Tree(data="hello world", id="2"))
    }

    fun testGetChildrenById() {
        testDatabase.trees["3"] = Tree(data="hello world", id="3")
        testDatabase.trees["2"] = Tree(data="hello world", id="2", children= mutableListOf("3"))
        assertEquals(testDatabase.getChildrenById("2")[0], Tree(data="hello world", id="3"))
    }

    fun testSaveAndLoadToStorage() {
        val mockEditor = mock(SharedPreferences.Editor::class.java)
        val mockPreferences = mock(SharedPreferences::class.java)

        `when`(mockEditor.putString(anyString(), anyString())).thenAnswer {
            val testTreeString = it.getArgument<String>(1)
            `when`(mockPreferences.getString(anyString(), anyString())).thenReturn(testTreeString)
            null
        }
        `when`(mockPreferences.edit()).thenReturn(mockEditor)

        testDatabase.addChild(Tree(data="hello"), ROOT_ID)
        testDatabase.saveToStorage(mockPreferences)

        testDatabase.trees.clear()
        testDatabase.loadTrees(TreeDatabase.loadFromStorage(mockPreferences))
        assertEquals(testDatabase.getChildrenById("1")[0], Tree("2", data="hello"))
    }
}