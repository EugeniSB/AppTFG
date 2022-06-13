package com.eugenisb.alphatest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.groups.CreateGroupNameActivity
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_create_group_members.*
import kotlinx.android.synthetic.main.create_group_add.view.*
import kotlinx.android.synthetic.main.create_group_delete.view.*
import java.util.ArrayList

class MultipleRecommednationContactsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var usersMap = mutableMapOf<String,String>()
    private var usersGroupMap = mutableMapOf<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_members)

        title = "Select users to recommend"
        contactsAddedGroupTextView.text = "Recommend to:"

        val movieName = intent.extras?.getString("movieName")
        val moviePoster = intent.extras?.getString("moviePoster")



        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            getContacts(userId)
        }

        nextGroupButton.setOnClickListener {
            if(usersGroupMap.isNotEmpty()){
                val nextGroupIntent = Intent(this, MultipleRecommendationCommentActivity::class.java).apply {
                    putExtra("movieName", movieName)
                    putExtra("moviePoster", moviePoster)
                    putExtra("usersGroupMap", usersGroupMap as HashMap<String,String>)
                }
                startActivity(nextGroupIntent)
            }
        }

        cancelGroupButton.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeIntent)
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
                //TODO AL FER EL REMOVE I EL ADD MIRAR DE COM ES VA FER A LES OPINIONS
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
            viewHolder.itemView.groupPromoteImageButton.visibility = View.GONE
            viewHolder.itemView.groupDeleteContactTextView.text = username
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: " + userId).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.groupDeleteContactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.groupDeleteContactImageView)
            }

            viewHolder.itemView.groupDeleteImageButton.setOnClickListener {
                println(usersMap)
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
            val contactsArray = document["contacts"] as ArrayList<String>

            if(contactsArray.isNotEmpty()) {
                for (id in contactsArray) {
                    db.collection("users").document(id).get().addOnSuccessListener {
                        usersMap[id] = it["username"] as String
                        adapter.add(GroupContactItem(id, it["username"] as String))
                    }
                }
                groupAddContactRecyclerView.adapter = adapter
            }else{
                noContactsGroupTextView.visibility = View.VISIBLE
            }
            //usersMap = document.data?.get("contacts") as MutableMap<String, String>
            /*
            for (key in usersMap.keys) {
                if (usersMap[key] != null) {
                    adapter.add(GroupContactItem(key, usersMap[key]!!))
                }
            }

             */


        }

    }

    private fun getContactsAfter() {

        val adapter = GroupAdapter<GroupieViewHolder>()
        val adapterAdded = GroupAdapter<GroupieViewHolder>()

        if(usersMap.isNotEmpty()) {
            for (key in usersMap.keys) {
                if (usersMap[key] != null) {
                    adapter.add(GroupContactItem(key, usersMap[key]!!))
                }
            }
        }else{
            noContactsGroupTextView.visibility = View.VISIBLE
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