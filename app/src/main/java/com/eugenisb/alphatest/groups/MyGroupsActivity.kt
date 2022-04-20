package com.eugenisb.alphatest.groups

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.contacts.ContactChatLogActivity
import com.eugenisb.alphatest.contacts.MyContactsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_groups.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import java.net.URI

class MyGroupsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_groups)

        title = "My groups"

        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            getGroups(userId)
        }

        createGroupfloatingActionButton.setOnClickListener {
            val createGroupIntent = Intent(this,CreateGroupMembersActivity::class.java)
            createGroupIntent.putExtra("userId",userId)
            startActivity(createGroupIntent)
        }
    }

    class GroupItem(val groupId: String, val groupName: String, val groupImg: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactTextView.text = groupName
            Picasso.get().load(groupImg).into(viewHolder.itemView.contactImageView)
        }

        override fun getLayout(): Int {
            return R.layout.my_contacts_item
        }
    }

    private fun getGroups(userId: String){
        db.collection("groups").whereArrayContains("membersId",userId).get()
            .addOnSuccessListener {
                val adapter = GroupAdapter<GroupieViewHolder>()
                for(document in it.documents){
                    var groupId = document.id
                    var groupName = document.get("name") as String
                    var groupImg = document.get("image") as String
                    adapter.add(GroupItem(groupId,groupName, groupImg))
                    Log.d(TAG, "MyGroups: $groupName")
                }
                adapter.setOnItemClickListener { item, view ->
                    val groupItem = item as GroupItem

                    val intentGroupChat = Intent(view.context, GroupChatLogActivity::class.java)
                    intentGroupChat.putExtra("groupId",item.groupId)
                    intentGroupChat.putExtra("groupName",item.groupName)
                    intentGroupChat.putExtra("groupImg",item.groupImg)
                    startActivity(intentGroupChat)
                }

                groupsRecyclerView.adapter = adapter
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