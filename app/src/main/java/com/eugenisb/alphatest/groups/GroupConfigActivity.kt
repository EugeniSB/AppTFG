package com.eugenisb.alphatest.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group_config.*
import kotlinx.android.synthetic.main.create_group_delete.view.*
import java.util.*

class GroupConfigActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var groupMembers: ArrayList<String>
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private var itsAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_config)

        val groupId = intent.extras?.getString("groupId")!!
        val groupAdmin = intent.extras?.getString("groupAdmin")
        var groupImage = intent.extras?.getString("groupImg")
        var groupName = intent.extras?.getString("groupName")!!

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
                putExtra("groupImg", groupImage)
                putExtra("groupName", groupName)
            }
            startActivity(addMembersIntent)
        }

        editGroupNameButton.setOnClickListener {
            editGroupNameButton.visibility = GONE
            groupConfigNameTextView.visibility = GONE
            editGroupNameEditText.setText(groupName)
            acceptEditedGroupName.visibility = VISIBLE
            editGroupNameEditText.visibility = VISIBLE

        }
        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                if (it != null) {
                    groupConfigImage.setImageURI(it)
                    groupImage = it.toString()
                    val imageName = UUID.randomUUID().toString()
                    val storageReference =
                        FirebaseStorage.getInstance().getReference("images/group_pics/$imageName")

                    storageReference.putFile(it).addOnSuccessListener {
                        Toast.makeText(this, "Successfuly uploaded image", Toast.LENGTH_SHORT)
                            .show()

                        storageReference.downloadUrl.addOnSuccessListener {
                            db.collection("groups").document(groupId).update(
                                "image",
                                it.toString()
                            )
                        }

                    }.addOnFailureListener {
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        )

        editGroupImageButton.setOnClickListener {
            getImage.launch("image/*")
        }


        acceptEditedGroupName.setOnClickListener {
            editGroup(editGroupNameEditText.text.toString(), groupId)
            groupName = editGroupNameEditText.text.toString()
        }

        leaveGroupButton.setOnClickListener {
            leaveGroup(groupId)
        }

    }

    private fun leaveGroup(groupId: String) {

        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(FirebaseAuth.getInstance().uid))
        groupMembers.remove(FirebaseAuth.getInstance().uid)
        if(groupMembers.size > 0){
            if(itsAdmin)
                db.collection("groups").document(groupId).update("creator", groupMembers[0])
        }else
            db.collection("groups").document(groupId).delete()

        //TODO TOAST DE ESTAS SEGUR QUE VOLS SORTIR
        val homeIntent = Intent(this, HomeActivity::class.java)
        startActivity(homeIntent)

        }

    private fun editGroup(newName: String, groupId: String) {

        db.collection("groups").document(groupId).update("name", newName)
        editGroupNameButton.visibility = VISIBLE
        groupConfigNameTextView.visibility = VISIBLE
        groupConfigNameTextView.text = newName

        acceptEditedGroupName.visibility = GONE
        editGroupNameEditText.visibility = GONE
    }

    inner class GroupMemberItem(val memberId: String, val memberUsername: String,
                                val memberImg: String, val groupId: String): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            if(!itsAdmin){
                viewHolder.itemView.groupDeleteImageButton.visibility = GONE
                viewHolder.itemView.groupPromoteImageButton.visibility = GONE
            }

            else if(memberId == FirebaseAuth.getInstance().uid){
                viewHolder.itemView.groupDeleteImageButton.visibility = GONE
                viewHolder.itemView.groupPromoteImageButton.visibility = GONE
            }



            viewHolder.itemView.groupDeleteContactTextView.text = memberUsername
            Picasso.get().load(memberImg).into(viewHolder.itemView.groupDeleteContactImageView)

            viewHolder.itemView.groupDeleteImageButton.setOnClickListener {

                //TODO POSAR TOAST ABANS DE BORRAR
                db.collection("groups").document(groupId).update("members",
                    FieldValue.arrayRemove(memberId))
                groupMembers.remove(memberId)
                adapter.removeGroupAtAdapterPosition(position)
                groupConfigRecyclerView.adapter = adapter
            }

            viewHolder.itemView.groupPromoteImageButton.setOnClickListener {

                //TODO POSAR TOAST ABANS DE FER PROMOTE
                db.collection("groups").document(groupId).update("creator", memberId)
                //val homeIntent = Intent(viewHolder.itemView.context, HomeActivity::class.java)
                //startActivity(homeIntent)
                onBackPressed()
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

        if(itsAdmin) {
            addGroupMembersImageButton.visibility = VISIBLE
            editGroupNameButton.visibility = VISIBLE
            editGroupImageButton.visibility = VISIBLE
        }

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