package com.eugenisb.alphatest.groups

import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.activity_group_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class GroupChatLogActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var groupId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat_log)


        groupChatLogRecyclerView.adapter = adapter
        val userId = FirebaseAuth.getInstance().uid

        groupId = intent.extras?.getString("groupId")
        val groupName = intent.extras?.getString("groupName")
        val groupImg =  intent.extras?.getString("groupImg")

        title = groupName

        listenForMessages(userId!!)

        chatLogGroupRecommendButton.setOnClickListener {
            val recommendGroupIntent = Intent(this,
                GroupRecommendationActivity::class.java)
            recommendGroupIntent.putExtra("groupId",groupId)
            recommendGroupIntent.putExtra("groupName",groupName)
            startActivity(recommendGroupIntent)
        }
    }

    private fun listenForMessages(userId: String){
        val ref = db.collection("group-recommendations/$groupId/$groupId").orderBy("Date",
            Query.Direction.ASCENDING)

        /*
        groupChatLogRecyclerView.postDelayed({
            groupChatLogRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }, 1000)

         */

        ref.addSnapshotListener { value, error ->
            if (error != null){
                Log.w(ContentValues.TAG, "Listen failed.", error);
                return@addSnapshotListener
            }

            for ( dc in value!!.documentChanges) {
                when (dc.type){
                    DocumentChange.Type.ADDED -> {
                        Log.d(ContentValues.TAG, "New message: ${dc.document.data}")
                        if (dc.document.data["from"] as String == userId) {
                            adapter.add(ChatFromItem(dc.document.data["name"] as String,
                            dc.document.data["comment"] as String, userId))
                        }else{
                            adapter.add(ChatToItem(dc.document.data["name"] as String,
                                dc.document.data["comment"] as String, dc.document.data["from"] as String))
                        }
                    }
                    DocumentChange.Type.MODIFIED -> ""
                    DocumentChange.Type.REMOVED -> ""
                }
            }
        }

    }

    class ChatToItem(val movieName: String, val movieComment: String, val userId: String): Item<GroupieViewHolder>(){
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

    class ChatFromItem(private val movieName: String, private val movieComment: String, val userId: String): Item<GroupieViewHolder>(){
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

}