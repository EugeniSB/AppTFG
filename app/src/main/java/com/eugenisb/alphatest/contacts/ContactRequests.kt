package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.adapters.RequestsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_requests.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactRequests : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: RequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_requests)


        title = "Contact Requests"

        val logged = verifyUserLoggedIn()

        var userId = ""

        if(logged){
            userId = FirebaseAuth.getInstance().uid!!
        }

        lifecycleScope.launch {
            var username = getUsername(userId)
            var requestsMap = getUsersRequestsSnapshot(userId)
            var requestsMapReversed = requestsMap.entries.associate { (k,v)-> v to k }
            adapterFun(requestsMapReversed as MutableMap<String, String>, username, userId)
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

    private suspend fun getUsersRequestsSnapshot(userId: String): Map<String,String> {
        val userDoc = getUserSnapshot(userId)
        return userDoc.get("contactRequests") as MutableMap<String, String>
    }

    private fun adapterFun(usersMap: MutableMap<String,String>, username: String, userId: String) {

        adapter = RequestsAdapter(
            this,
            usersMap.keys.toMutableList(),
            usersMap,
            username,
            userId
        )

        listViewRequests.adapter = adapter
        listViewRequests.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Toast.makeText(applicationContext, parent?.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show()
        }
        listViewRequests.emptyView = noRequestsTextView
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)

        val search = menu?.findItem(R.id.nav_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search for contact requests"

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