package com.eugenisb.alphatest.contacts

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_contacts.addContactsFloatingActionButton
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import kotlinx.coroutines.tasks.await
import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat


class ContactsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val sdf = SimpleDateFormat("dd/MM/YY HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        verifyUserLoggedIn()

        val userId = FirebaseAuth.getInstance().uid
        getContacts(userId!!)



    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intentAuth = Intent(activity, AuthActivity::class.java)
            intentAuth.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }


    inner class ContactItem(val contactId: String, val username: String, val userId: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactTextView.text = username
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: " + contactId).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.contactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile).into(viewHolder.itemView.contactImageView)
            }
            val ref = db.collection("user-recommendations/$userId/$contactId").orderBy("Date",
                Query.Direction.ASCENDING)

            ref.addSnapshotListener { value, error ->
                if (error != null){
                    Log.w(ContentValues.TAG, "Listen failed.", error);
                    return@addSnapshotListener
                }

                for ( dc in value!!.documentChanges) {
                    when (dc.type){
                        DocumentChange.Type.ADDED -> {
                            viewHolder.itemView.lastMessageTextView.text =
                                dc.document.data["name"] as String
                            val date = dc.document.data["Date"] as com.google.firebase.Timestamp
                            val finalDate = date.toDate()
                            viewHolder.itemView.lastMessageTimeTextView.text =
                                sdf.format(finalDate).toString()
                        }

                        DocumentChange.Type.MODIFIED -> {
                            viewHolder.itemView.lastMessageTextView.text =
                                dc.document.data["name"] as String
                            val date = dc.document.data["Date"] as com.google.firebase.Timestamp
                            val finalDate = date.toDate()
                            viewHolder.itemView.lastMessageTimeTextView.text =
                                sdf.format(finalDate).toString()
                        }

                        DocumentChange.Type.REMOVED -> {
                            viewHolder.itemView.lastMessageTextView.text =
                                dc.document.data["name"] as String
                            val date = dc.document.data["Date"] as com.google.firebase.Timestamp
                            val finalDate = date.toDate()
                            viewHolder.itemView.lastMessageTimeTextView.text =
                                sdf.format(finalDate).toString()
                        }
                    }
                }
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
                    adapter.add(ContactItem(key, usersMap[key]!!, userId))
                }
            }
            adapter.setOnItemClickListener{ item,view ->
                val contactItem = item as ContactItem

                val intentChatLog = Intent(view.context, ContactChatLogActivity::class.java)
                intentChatLog.putExtra("contactId",item.contactId)
                intentChatLog.putExtra("contactUsername",item.username)
                startActivity(intentChatLog)
            }
            fragmentRecyclerView.adapter = adapter
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addContactsFloatingActionButton.setOnClickListener {
            val addContactsIntent = Intent(activity, AddContactActivity::class.java).apply {
                putExtra("userId", FirebaseAuth.getInstance().uid)

            }
            startActivity(addContactsIntent)
        }
    }
}