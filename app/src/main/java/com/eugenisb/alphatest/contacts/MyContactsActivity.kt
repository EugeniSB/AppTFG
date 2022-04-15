package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_contacts.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*

class MyContactsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        title = "Contacts"

        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            getContacts(userId)
        }
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
            FirebaseStorage.getInstance().reference.child(
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

        db.collection("users").document(userId).get().addOnSuccessListener {
            document -> Log.d("User","Users contacts: " + document.data?.get("contacts"))
            val adapter = GroupAdapter<GroupieViewHolder>()
            val usersMap = document.data?.get("contacts") as Map<String,String>
            for (key in usersMap.keys){
                    if(usersMap[key] != null){
                        adapter.add(ContactItem(key,usersMap[key]!!))
                    }
            }
            adapter.setOnItemClickListener{ item,view ->
                val contactItem = item as ContactItem

                val intentChatLog = Intent(view.context, ContactChatLogActivity::class.java)
                intentChatLog.putExtra("contactId",item.userId)
                intentChatLog.putExtra("contactUsername",item.username)
                startActivity(intentChatLog)
            }
            contactsRecyclerView.adapter = adapter
        }


    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
    }

}

