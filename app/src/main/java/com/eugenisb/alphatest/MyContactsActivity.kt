package com.eugenisb.alphatest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_contacts.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*

class MyContactsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        title = "Contacts"

        val userId = verifyUserLoggedIn()

        /*
        val adapter = GroupAdapter<GroupieViewHolder>()

        adapter.add(ContactItem())
        adapter.add(ContactItem())
        adapter.add(ContactItem())

        contactsRecyclerView.adapter=adapter*/

        getContacts(userId)
        addContactsfloatingActionButton.setOnClickListener {
            val addContactsIntent = Intent(this, AddContactActivity::class.java).apply {
                putExtra("userId", userId)

            }
            startActivity(addContactsIntent)
        }
    }


    class ContactItem(val userId: String, val username: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactTextView.text = username
            val imgReference = FirebaseStorage.getInstance().getReference().child(
                "images/profile_pics/Profile_picture_of: " + userId).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.contactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.contactImageView)
            }
        }

        override fun getLayout(): Int {
            return R.layout.my_contacts_item
        }
    }

    private fun getContacts(userId: String) {

        val ref = db.collection("users").document(userId).get().addOnSuccessListener {
            document -> Log.d("User","Users contacts: " + document.data?.get("contacts"))
            val adapter = GroupAdapter<GroupieViewHolder>()
            val usersMap = document.data?.get("contacts") as Map<String,String>
            for (key in usersMap.keys){
                //val user = Contact()
                    if(usersMap[key] != null){
                        adapter.add(ContactItem(key,usersMap[key]!!))
                    }
            }

            contactsRecyclerView.adapter = adapter

        }


    }

    private fun verifyUserLoggedIn() : String{
        val uid = FirebaseAuth.getInstance().uid ?: ""
        if(uid == null){
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
        return uid
    }

    class Contact(val uid: String, val username: String){
        constructor() : this("","")
    }
}

