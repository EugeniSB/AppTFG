package com.eugenisb.alphatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore

class AddContactActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ArrayAdapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        title = "Add contact"

        val bundle = intent.extras
        val email = bundle?.getString("email")
        //val usernames = bundle?.getStringArrayList("usernames")
        //////val usernamesMap = bundle?.getSerializable("usernames")


        getUsernames(){
            val usersMap = it
            val keys = usersMap?.keys.toTypedArray()
            val usernames = keys.toList()
            adapterFun(usernames)
        }

    }

    private fun adapterFun(usernames: List<String>) {
        adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1,
            usernames ?: emptyList())

        var usersList = findViewById<ListView>(R.id.listViewUsers)

        usersList.adapter = adapter
        usersList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(applicationContext, parent?.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show()
        }
        usersList.emptyView = findViewById<TextView>(R.id.emptytextView)
    }

    private fun getUsernames(myCallback: (MutableMap<String,String>) -> Unit) {

        val usersMap = mutableMapOf<String,String>()

        db.collection("users").whereNotEqualTo("username", null).get().addOnSuccessListener {
                documents -> for (document in documents) {
            usersMap[document.getString("username").toString()] = document.id.toString()
            Log.d("usersMapDocs","${document.getString("username")} : ${document.id.toString()}")
        }
            myCallback(usersMap)
        }.addOnFailureListener {
            Log.w("usersMapDocs","Error getting documents")
        }


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