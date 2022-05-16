package com.eugenisb.alphatest.groups

import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.eugenisb.alphatest.contacts.ContactProfileActivity
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
import kotlinx.android.synthetic.main.chat_popup.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class GroupChatLogActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var groupId : String
    private lateinit var groupAdmin: String

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat_log)


        groupChatLogRecyclerView.adapter = adapter
        val userId = FirebaseAuth.getInstance().uid

        groupId = intent.extras?.getString("groupId")!!
        val groupName = intent.extras?.getString("groupName")
        val groupImg =  intent.extras?.getString("groupImg")

        title = groupName

        getGroupAdmin()

        listenForMessages(userId!!)

        chatLogGroupRecommendButton.setOnClickListener {
            val recommendGroupIntent = Intent(this,
                SearchMovieAPIActivity::class.java)
            //recommendGroupIntent.putExtra("groupId",groupId)
           // recommendGroupIntent.putExtra("groupName",groupName)
            recommendGroupIntent.putExtra("contactId",groupId)
            recommendGroupIntent.putExtra("contactUsername",groupName)
            recommendGroupIntent.putExtra("screen","group")
            startActivity(recommendGroupIntent)
        }
    }

    private fun getGroupAdmin() {
        db.collection("groups").document(groupId).get().addOnSuccessListener {
            groupAdmin = it["creator"] as String
        }
    }

    private fun listenForMessages(userId: String){
        val ref = db.collection("group-recommendations/$groupId/$groupId").orderBy("Date",
            Query.Direction.ASCENDING)


        groupChatLogRecyclerView.postDelayed({
            groupChatLogRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }, 1000)



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
                            dc.document.data["comment"] as String, userId, dc.document.id
                                ,dc.document.data["poster"] as String))
                        }else{
                            adapter.add(ChatToItem(dc.document.data["name"] as String,
                                dc.document.data["comment"] as String,
                                dc.document.data["from"] as String,
                                dc.document.data["poster"] as String))
                        }
                    }
                    DocumentChange.Type.MODIFIED -> ""
                    DocumentChange.Type.REMOVED -> ""
                }
            }
        }

    }

    inner class ChatToItem(val movieName: String, val movieComment: String,
                     val userId: String, val moviePoster: String): Item<GroupieViewHolder>(){
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

            viewHolder.itemView.fromLayout.setOnClickListener {
                layoutInf =
                    applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.chat_popup, null) as ViewGroup

                Picasso.get().load(moviePoster).into(container.popUpChatImageView)
                container.movieChatNamePopUpTextView.text = movieName

                popUp = PopupWindow(container, 800, 1350, true)
                popUp.showAtLocation(myGroupChatLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                myGroupChatLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myGroupChatLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

            }
        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }

    inner class ChatFromItem(private val movieName: String, private val movieComment: String,
                       val userId: String, val messageId: String, val moviePoster: String): Item<GroupieViewHolder>(){

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

            viewHolder.itemView.deleteChatRowImageButton.setOnClickListener {
                db.collection("group-recommendations/$groupId/" +
                        "$groupId").document("$messageId").delete()
                adapter.removeGroupAtAdapterPosition(position)

            }

            viewHolder.itemView.toLayout.setOnClickListener {
                layoutInf =
                    applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.chat_popup, null) as ViewGroup

                Picasso.get().load(moviePoster).into(container.popUpChatImageView)
                container.movieChatNamePopUpTextView.text = movieName

                popUp = PopupWindow(container, 800, 1350, true)
                popUp.showAtLocation(myGroupChatLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                myGroupChatLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myGroupChatLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

            }

        }

        override fun getLayout(): Int {
            return R.layout.chat_to_row
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")
        when (item.itemId){
            R.id.group_options ->{
                val groupConfigIntent = Intent(this, GroupConfigActivity::class.java)
                groupConfigIntent.putExtra("groupId", groupId)
                groupConfigIntent.putExtra("groupAdmin", groupAdmin)
                groupConfigIntent.putExtra("groupName", intent.extras?.getString("groupName"))
                groupConfigIntent.putExtra("groupImg", intent.extras?.getString("groupImg"))
                startActivity(groupConfigIntent)


            }
        }
        return super.onOptionsItemSelected(item)
    }

}