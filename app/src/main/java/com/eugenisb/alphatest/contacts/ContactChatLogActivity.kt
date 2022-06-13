package com.eugenisb.alphatest.contacts

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
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
import kotlinx.android.synthetic.main.activity_my_opinions.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_popup.*
import kotlinx.android.synthetic.main.chat_popup.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import kotlinx.android.synthetic.main.opinion_popup.view.*

class ContactChatLogActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var contactId : String? = null
    private var userId : String? = null

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chat_log)

        contactChatLogRecyclerView.adapter = adapter

        contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")
        //USER LOGGED IN?????
        userId = FirebaseAuth.getInstance().uid



        title = contactUsername

        listenForMessages()

        chatLogContactRecommendButton.setOnClickListener {
            val recommendContactIntent = Intent(this,
                SearchMovieAPIActivity::class.java)
            recommendContactIntent.putExtra("contactId",contactId)
            recommendContactIntent.putExtra("contactUsername",contactUsername)
            recommendContactIntent.putExtra("screen","user")
            startActivity(recommendContactIntent)
        }

    }

    private fun listenForMessages(){
        val ref = db.collection("user-recommendations/$userId/$contactId").orderBy("Date",
            Query.Direction.ASCENDING)

        contactChatLogRecyclerView.postDelayed({
            contactChatLogRecyclerView.scrollToPosition(adapter.itemCount - 1)
        }, 100) //TODO AIXO DE DELAY MILIS MIRAR A VEURE QUE

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
                            dc.document.data["comment"] as String, userId!!, dc.document.id,
                            dc.document.data["poster"] as String))
                        }else{
                            adapter.add(ChatToItem(dc.document.data["name"] as String,
                                dc.document.data["comment"] as String, contactId!!,
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
                popUp.showAtLocation(myChatLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                myChatLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myChatLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

            }

            viewHolder.itemView.fromImageView.setOnClickListener{
                val contactProfileIntent = Intent(it.context, ContactProfileActivity::class.java)
                contactProfileIntent.putExtra("contactId", userId)
                startActivity(contactProfileIntent)
            }

        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row
        }
    }

    inner class ChatFromItem(private val movieName: String, private val movieComment: String, val userId: String,
    val messageId: String, val moviePoster: String): Item<GroupieViewHolder>(){

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
                db.collection("user-recommendations/$userId/$contactId").document("$messageId").delete()
                db.collection("user-recommendations/$contactId/$userId").document("$messageId").delete()
                adapter.removeGroupAtAdapterPosition(position)

            }

            viewHolder.itemView.toLayout.setOnClickListener {
                layoutInf =
                    applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.chat_popup, null) as ViewGroup

                Picasso.get().load(moviePoster).into(container.popUpChatImageView)
                container.movieChatNamePopUpTextView.text = movieName

                popUp = PopupWindow(container, 800, 1350, true)
                popUp.showAtLocation(myChatLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                myChatLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myChatLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

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
        //val contactId = intent.extras?.getString("contactId")
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