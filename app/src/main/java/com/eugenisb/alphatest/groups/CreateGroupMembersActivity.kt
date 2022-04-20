package com.eugenisb.alphatest.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.contacts.MyContactsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_create_group_members.*
import kotlinx.android.synthetic.main.activity_my_contacts.*
import kotlinx.android.synthetic.main.create_group_add.*
import kotlinx.android.synthetic.main.create_group_add.view.*
import kotlinx.android.synthetic.main.create_group_delete.view.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*

class CreateGroupMembersActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var usersMap = mutableMapOf<String,String>()
    private var usersGroupMap = mutableMapOf<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_members)

        title = "Group Members"

        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            getContacts(userId)
        }

        nextGroupButton.setOnClickListener {
            if(usersGroupMap.isNotEmpty()){
                val nextGroupIntent = Intent(this, CreateGroupNameActivity::class.java).apply {
                    putExtra("usersGroupMap", usersGroupMap as HashMap<String,String>)
                }
                startActivity(nextGroupIntent)
            }
        }

        cancelGroupButton.setOnClickListener {
            onBackPressed()
        }
    }

    inner class GroupContactItem(val userId: String, val username: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.groupAddContactTextView.text = username
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: " + userId).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.groupAddContactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.groupAddContactImageView)
            }


            viewHolder.itemView.groupAddImageButton.setOnClickListener {
                usersGroupMap[userId] = username
                usersMap.remove(userId)
                getContactsAfter()
            }
        }


        override fun getLayout(): Int {
            return R.layout.create_group_add
        }
    }

    inner class GroupAddedContactItem(val userId: String, val username: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.groupDeleteContactTextView.text = username
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: " + userId).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.groupDeleteContactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.groupDeleteContactImageView)
            }

            viewHolder.itemView.groupDeleteImageButton.setOnClickListener {
                usersMap[userId] = username
                usersGroupMap.remove(userId)
                getContactsAfter()
            }
        }


        override fun getLayout(): Int {
            return R.layout.create_group_delete
        }
    }

    private fun getContacts(userId: String) {

        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            Log.d("User", "Users contacts: " + document.data?.get("contacts"))
            val adapter = GroupAdapter<GroupieViewHolder>()
            usersMap = document.data?.get("contacts") as MutableMap<String, String>
            for (key in usersMap.keys) {
                if (usersMap[key] != null) {
                    adapter.add(GroupContactItem(key, usersMap[key]!!))
                }
            }

            groupAddContactRecyclerView.adapter = adapter
        }

    }

    private fun getContactsAfter() {

        val adapter = GroupAdapter<GroupieViewHolder>()
        val adapterAdded = GroupAdapter<GroupieViewHolder>()

        for (key in usersMap.keys) {
            if (usersMap[key] != null) {
                adapter.add(GroupContactItem(key, usersMap[key]!!))
            }
        }

        for (key in usersGroupMap.keys) {
            if (usersGroupMap[key] != null) {
                adapterAdded.add(GroupAddedContactItem(key, usersGroupMap[key]!!))
            }
        }

        groupAddContactRecyclerView.adapter = adapter
        groupAddedRecyclerView.adapter = adapterAdded
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