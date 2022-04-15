package com.eugenisb.alphatest.contacts

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_contact_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*

class ContactChatLogActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var contactId : String? = null
    private var userId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chat_log)

        contactChatLogRecyclerView.adapter = adapter

        contactId = intent.extras?.getString("contactId")
        //USER LOGGED IN?????
        userId = FirebaseAuth.getInstance().uid

        val contactUsername = intent.extras?.getString("contactUsername")

        title = contactUsername

        listenForMessages()

        chatLogContactRecommendButton.setOnClickListener {
            val recommendContactIntent = Intent(this,
                ContactRecommendationActivity::class.java)
            recommendContactIntent.putExtra("contactId",contactId)
            recommendContactIntent.putExtra("contactUsername",contactUsername)
            startActivity(recommendContactIntent)
        }

    }

    private fun listenForMessages(){
        val ref = db.collection("user-recommendations/$userId/$contactId").orderBy("Date",
            Query.Direction.ASCENDING)

        contactChatLogRecyclerView.postDelayed({
            contactChatLogRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }, 1000)

        ref.addSnapshotListener { value, error ->
            if (error != null){
                Log.w(TAG, "Listen failed.", error);
                return@addSnapshotListener
            }

            for ( dc in value!!.documentChanges) {
                when (dc.type){
                    DocumentChange.Type.ADDED -> {
                        Log.d(TAG, "New message: ${dc.document.data}")
                        if (dc.document.data["from"] as String == FirebaseAuth.getInstance().uid) {
                            adapter.add(ChatFromItem(dc.document.data["name"] as String,
                            dc.document.data["comment"] as String, userId!!))
                        }else{
                            adapter.add(ChatToItem(dc.document.data["name"] as String,
                                dc.document.data["comment"] as String, contactId!!))
                        }
                    }
                    DocumentChange.Type.MODIFIED -> ""
                    DocumentChange.Type.REMOVED -> ""
                }
            }
        }

    }

    class ChatFromItem(val movieName: String, val movieComment: String, val userId: String): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.fromTitleTextView.text = "$movieName:"
            viewHolder.itemView.fromTitleTextView.paintFlags =
                viewHolder.itemView.fromTitleTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            viewHolder.itemView.fromCommentTextView.text = movieComment
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: $userId"
            ).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.fromImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.fromImageView)
            }
        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }

    class ChatToItem(private val movieName: String, private val movieComment: String, val userId: String): Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.toTitleTextView.text = "$movieName:"
            viewHolder.itemView.toTitleTextView.paintFlags =
                viewHolder.itemView.toTitleTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            viewHolder.itemView.toCommentTextView.text = movieComment
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: $userId"
            ).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.toImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.toImageView)
            }

        }

        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")
        when (item.itemId){
            R.id.contact_profile ->{
                val contactProfileIntent = Intent(this, ContactProfileActivity::class.java)
                contactProfileIntent.putExtra("contactId", contactId)
                contactProfileIntent.putExtra("contactUsername", contactUsername)
                startActivity(contactProfileIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class ChatMessage(val id:String, val movieName: String, val movieComment: String,
                      val fromId:String, val toId: String, val timeStamp: Long){
        constructor() : this("","","","","",-1)
    }
}