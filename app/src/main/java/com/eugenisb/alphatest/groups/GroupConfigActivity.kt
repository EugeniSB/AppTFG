package com.eugenisb.alphatest.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group_config.*
import kotlinx.android.synthetic.main.create_group_delete.view.*
import java.util.ArrayList

class GroupConfigActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var groupMembers: ArrayList<String>
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var itsAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_config)

        val groupId = intent.extras?.getString("groupId")
        val groupAdmin = intent.extras?.getString("groupAdmin")
        val groupImage = intent.extras?.getString("groupImage")
        val groupName = intent.extras?.getString("groupName")

        if(FirebaseAuth.getInstance().uid == groupAdmin)
            itsAdmin = true

        title = "Group Config"

        if(groupId != null && groupAdmin != null && groupImage != null &&
            groupName != null)
            getGroup(groupId, groupAdmin, groupImage, groupName)

        addGroupMembersImageButton.setOnClickListener {
            val addMembersIntent = Intent(this, AddMembersActivity::class.java).apply {
                putExtra("groupId", groupId)
                putExtra("groupMembers", groupMembers)
                putExtra("groupImage", groupImage)
                putExtra("groupName", groupName)
            }
            startActivity(addMembersIntent)
        }

    }

    inner class GroupMemberItem(val memberId: String, val memberUsername: String,
                                val memberImg: String, val groupId: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            if(!itsAdmin)
                viewHolder.itemView.groupDeleteImageButton.visibility = GONE
            else if(memberId == FirebaseAuth.getInstance().uid)
                viewHolder.itemView.groupDeleteImageButton.visibility = GONE


            viewHolder.itemView.groupDeleteContactTextView.text = memberUsername
            Picasso.get().load(memberImg).into(viewHolder.itemView.groupDeleteContactImageView)

            viewHolder.itemView.groupDeleteImageButton.setOnClickListener {

                //TODO POSAR TOAST ABANS DE BORRAR
                db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(memberId))
                groupMembers.remove(memberId)
                adapter.removeGroupAtAdapterPosition(position)
            }

        }


        override fun getLayout(): Int {
            return R.layout.create_group_delete
        }
    }

    private fun getGroup(groupId: String, groupAdmin: String, groupImage: String,
                         groupName: String) {

        Picasso.get().load(groupImage).into(groupConfigImage)
        groupConfigNameTextView.text = groupName

        if(itsAdmin)
            addGroupMembersImageButton.visibility = VISIBLE

        db.collection("groups").document(groupId).get().addOnSuccessListener { document ->

            groupMembers = document["members"] as ArrayList<String>
            for(id in groupMembers){
                db.collection("users").document(id).get().addOnSuccessListener {
                    adapter.add(GroupMemberItem(id, it["username"] as String,
                        it["userImg"] as String, groupId))
                }
            }

            groupConfigRecyclerView.adapter = adapter
        }

    }

}