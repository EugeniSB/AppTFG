package com.eugenisb.alphatest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore

class MyContactsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        title = "Contacts"

        val bundle = intent.extras
        val userId = bundle?.getString("userId")


        /*
        val usersMap = mutableMapOf<String,String>()


        db.collection("users").whereNotEqualTo("username", null).get().addOnSuccessListener {
            documents -> for (document in documents) {
                usersMap.put(document.getString("username").toString(),document.id.toString())
                Log.d("usersMapDocs","${document.getString("username")} : ${document.id.toString()}")
            }
        }.addOnFailureListener {
            Log.w("usersMapDocs","Error getting documents")
        }
        */

        ///////////////var usersMap = getUsers()


        //Log.d("TestMap2", "Esto no va")

        /////////////////Log.d("usersMapFinal", usersMap["Test2"].toString())

        val addContactsButton = findViewById<Button>(R.id.addcontactsButton)
        addContactsButton.setOnClickListener {
            //val usernames = usersMap.keys.toTypedArray()

            val addContactsIntent = Intent(this, AddContactActivity::class.java).apply {
                putExtra("userId", userId)
                /////////////putExtra("usernames", usersMap)
            }
            startActivity(addContactsIntent)
            //findViewById<TextView>(R.id.testtextView).setText(usersMap["Test2"] as String)
            //findViewById<TextView>(R.id.testtextView2).setText(usernames[1] as String)



        }
    }

    /*
    private fun getUsers() : MutableMap<String,String>{

        val usersMap = hashMapOf<String,String>()

        db.collection("users").whereNotEqualTo("username", null).get().addOnSuccessListener {
                documents -> for (document in documents) {
            usersMap[document.getString("username").toString()] = document.id.toString()
            Log.d("usersMapDocs","${document.getString("username")} : ${document.id.toString()}")
        }

        }.addOnFailureListener {
            Log.w("usersMapDocs","Error getting documents")
        }

        return usersMap

    }
    */




    /*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.searchbar,menu)
        val itemSearch = menu?.findItem(R.id.menu_search)
        if (itemSearch != null) {
            val searchView = itemSearch.actionView as SearchView
            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }

            })
        }


        return super.onCreateOptionsMenu(menu)
    }
    */
}

