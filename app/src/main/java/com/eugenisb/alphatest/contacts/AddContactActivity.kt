package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.adapters.SearchableAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_add_contact.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

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
        var usersAddedMap : Set<String>


        if(logged){
            userId = FirebaseAuth.getInstance().uid!!
        }

        lifecycleScope.launch {
            username = getUsername(userId)
            usersMap = getUsernames(username) //Pillar tots els users
            usersAddedMap = getUsersAddedSnapshot(userId) //Pillar tots els users agregats o en solicitud
            usersMap.keys.removeAll(usersAddedMap)// Treure els agregats de la llista de tots per ID
            val names = usersMap.values.toTypedArray()
            usersMap = usersMap.entries.associate { (k,v)-> v to k } as MutableMap<String, String>
            val usernames = names.toMutableList()
            adapterFun(usernames, usersMap, username)
        }

        contactRequestsbutton.setOnClickListener {
            val contactRequestsIntent = Intent(this, ContactRequests::class.java)
            startActivity(contactRequestsIntent)
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

    private suspend fun getUserSnapshot(userId: String): DocumentSnapshot {
        return db.collection("users").document(userId).get().await()
    }

    private suspend fun getUsername(userId: String): String{
        val userDoc = getUserSnapshot(userId)
        return userDoc.getString("username")!!
    }

    private suspend fun getUsersSnapshot(username: String): QuerySnapshot {
        return db.collection("users").whereNotEqualTo("username", username).get().await()
    }

    private suspend fun getUsernames(username: String): MutableMap<String,String>{
        val usersDoc = getUsersSnapshot(username)
        val usersMap = mutableMapOf<String,String>()

        for(document in usersDoc.documents){
            //usersMap[document.getString("username").toString()] = document.id
            usersMap[document.id] = document.getString("username").toString()
        }
        return usersMap
    }

    private suspend fun getUsersAddedSnapshot(userId: String): Set<String> {
        val userDoc = getUserSnapshot(userId)
        val requests = userDoc.get("contactRequestsSent") as Map<String, String>
        val contacts = userDoc.get("contacts") as ArrayList<String>
        val requestsRecived = userDoc.get("contactRequests") as Map<String, String>

        return requests.keys + contacts + requestsRecived.keys
    }


    private fun adapterFun(usernames: List<String>, usersMap: MutableMap<String,String>, username: String) {

        adapter = SearchableAdapter(
            this,
            usernames,
            usersMap,
            username
        )

        listViewUsers.adapter = adapter
        listViewUsers.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(applicationContext, parent?.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show()
        }
        listViewUsers.emptyView = emptytextView
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