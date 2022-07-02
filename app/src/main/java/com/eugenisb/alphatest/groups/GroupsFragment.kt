package com.eugenisb.alphatest.groups

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_groups.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import java.text.SimpleDateFormat

class GroupsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val sdf = SimpleDateFormat("dd/MM/YY HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verifyUserLoggedIn()

        val userId = FirebaseAuth.getInstance().uid

        if(userId != null) {
            getGroups(userId!!)
        }

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
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    inner class GroupItem(val groupId: String, val groupName: String, val groupImg: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactTextView.text = groupName
            Picasso.get().load(groupImg).into(viewHolder.itemView.contactImageView)

            val ref = db.collection("group-recommendations/$groupId/$groupId").orderBy("Date",
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

    private fun getGroups(userId: String){
        db.collection("groups").whereArrayContains("members",userId).get()
            .addOnSuccessListener {
                val adapter = GroupAdapter<GroupieViewHolder>()

                if(it.documents.isNotEmpty()){
                    for(document in it.documents){
                        var groupId = document.id
                        var groupName = document.get("name") as String
                        var groupImg = document.get("image") as String
                        adapter.add(GroupItem(groupId,groupName, groupImg))
                        Log.d(ContentValues.TAG, "MyGroups: $groupName")
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val groupItem = item as GroupItem

                        val intentGroupChat = Intent(view.context, GroupChatLogActivity::class.java)
                        intentGroupChat.putExtra("groupId",item.groupId)
                        intentGroupChat.putExtra("groupName",item.groupName)
                        intentGroupChat.putExtra("groupImg",item.groupImg)
                        startActivity(intentGroupChat)
                    }

                    fragment2RecyclerView.adapter = adapter
                }else{
                    fragment2RecyclerView.visibility = View.GONE
                    noGroupsTextView.visibility = View.VISIBLE
                }

            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createGroupFloatingActionButton.setOnClickListener {
            val createGroupIntent = Intent(activity,CreateGroupMembersActivity::class.java)
            createGroupIntent.putExtra("userId", FirebaseAuth.getInstance().uid)
            startActivity(createGroupIntent)
        }
    }

}