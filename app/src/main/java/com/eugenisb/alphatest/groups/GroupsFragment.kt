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
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_groups.*
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*

class GroupsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verifyUserLoggedIn()

        val userId = FirebaseAuth.getInstance().uid

        getGroups(userId!!)

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