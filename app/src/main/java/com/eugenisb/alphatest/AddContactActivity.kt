package com.eugenisb.alphatest

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add_contact.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.MappedByteBuffer
import java.util.*

class AddContactActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: SearchableAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        title = "Add contact"

        val logged = verifyUserLoggedIn()
        var userId = ""
        var username = ""
        var usersMap = mutableMapOf<String,String>()
        var usersAddedMap = mapOf<String,String>()


        if(logged){
            userId = FirebaseAuth.getInstance().uid!!
        }

        lifecycleScope.launch {
            username = getUsername(userId)
            usersMap = getUsernames(username)
            usersAddedMap = getUsersAddedSnapshot(userId)
            usersMap.keys.removeAll(usersAddedMap.values)
            val keys = usersMap.keys.toTypedArray()
            val usernames = keys.toMutableList()
            adapterFun(usernames, usersMap, username)
        }
    }

    private fun verifyUserLoggedIn() : Boolean{
        val uid = FirebaseAuth.getInstance().uid
        val logged = true
        if(uid == null){
            val logged = false
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
        return logged
    }

    private suspend fun getUsernameSnapshot(userId: String): DocumentSnapshot {
        return db.collection("users").document(userId).get().await()
    }

    private suspend fun getUsername(userId: String): String{
        val userDoc = getUsernameSnapshot(userId)
        return userDoc.getString("username")!!
    }

    private suspend fun getUsersSnapshot(username: String): QuerySnapshot {
        return db.collection("users").whereNotEqualTo("username", username).get().await()
    }

    private suspend fun getUsernames(username: String): MutableMap<String,String>{
        val usersDoc = getUsersSnapshot(username)
        val usersMap = mutableMapOf<String,String>()

        for(document in usersDoc.documents){
            usersMap[document.getString("username").toString()] = document.id
        }
        return usersMap
    }

    private suspend fun getUsersAddedSnapshot(userId: String): Map<String,String> {
        val userDoc = getUsernameSnapshot(userId)
        val requests = userDoc.get("userRequestsSent") as Map<String, String>
        val contacts = userDoc.get("contacts") as Map<String, String>
        return requests + contacts
    }


    private fun adapterFun(usernames: List<String>, usersMap: MutableMap<String,String>, username: String) {
        /*adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,
            usernames ?: emptyList())*/

        adapter = SearchableAdapter(this,usernames, usersMap, username)

        var usersList = findViewById<ListView>(R.id.listViewUsers)

        usersList.adapter = adapter
        usersList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(applicationContext, parent?.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show()
        }
        usersList.emptyView = findViewById<TextView>(R.id.emptytextView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)

        val search = menu?.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search users by username"

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}