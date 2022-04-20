package com.eugenisb.alphatest.groups

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_create_group_name.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import java.util.*

class CreateGroupNameActivity : AppCompatActivity() {

    lateinit var URIimage : Uri
    lateinit var username : String
    private var imageUpdated : Boolean = false
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group_name)

        title = "Group Configuration"

        var groupContacts = intent.extras?.getSerializable("usersGroupMap") as MutableMap<String, String>
        var userId = FirebaseAuth.getInstance().uid


        if(userId != null){
            getGroupContacts(groupContacts as Map<String, String>)
            getUsername(userId)
        }

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                circleAvatarGroup.setImageURI(it)
                URIimage = it
                imageUpdated = true

            }
        )

        uploadGroupImageButton.setOnClickListener {
            getImage.launch("image/*")

        }

        createGroupButton.setOnClickListener {
            if(groupNameEditText.text.isNotEmpty()){
                createGroup(groupContacts)

            }
        }

        backGroupButton.setOnClickListener {
            onBackPressed()
        }
    }

    class GroupContactItem(val userId: String, val username: String) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactTextView.text = username
            FirebaseStorage.getInstance().reference.child(
                "images/profile_pics/Profile_picture_of: " + userId
            ).downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(viewHolder.itemView.contactImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.profile)
                    .into(viewHolder.itemView.contactImageView)
            }

        }

        override fun getLayout(): Int {
            return R.layout.my_contacts_item
        }
    }

    private fun getGroupContacts(groupContacts: Map<String, String>) {

        val adapter = GroupAdapter<GroupieViewHolder>()

        for (key in groupContacts.keys) {
            if (groupContacts[key] != null) {
                adapter.add(GroupContactItem(key, groupContacts[key]!!))
            }
        }

        groupAdded2RecyclerView.adapter = adapter
    }

    private fun getUsername(userId: String){
        db.collection("users").document(userId).get().addOnSuccessListener {
            username= it.get("username") as String
        }
    }

    private fun createGroup(groupContacts: MutableMap<String, String>) {

        val groupName = groupNameEditText.text.toString()
        val userId = FirebaseAuth.getInstance().uid
        val groupImageUrl = "https://firebasestorage.googleapis.com/v0/b/alphatest-58fe9.appspot.com/o/images%2FProfile.png?alt=media&token=37a92d5b-ee2d-46b0-83a7-f72dc8db1e77"

        groupContacts[userId!!] = username

        val groupIds = groupContacts.keys.toTypedArray().toList()


        if(imageUpdated){
            val imageName =  UUID.randomUUID().toString()
            val storageReference = FirebaseStorage.getInstance().getReference("images/group_pics/$imageName")

            storageReference.putFile(URIimage).addOnSuccessListener {
                Toast.makeText(this, "Successfuly uploaded image", Toast.LENGTH_SHORT).show()

                storageReference.downloadUrl.addOnSuccessListener {
                    val groupImageUrl = it.toString()

                    val group = hashMapOf(
                        "name" to groupName,
                        "creator" to userId,
                        "members" to groupContacts,
                        "membersId" to groupIds,
                        "image" to groupImageUrl
                    )

                    db.collection("groups").add(group)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
        }else
        {
            val group = hashMapOf(
                "name" to groupName,
                "creator" to userId,
                "members" to groupContacts,
                "membersId" to groupIds,
                "image" to groupImageUrl
            )

            db.collection("groups").add(group)
        }

        val groupsIntent = Intent(this, MyGroupsActivity::class.java)
        groupsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(groupsIntent)

    }

}
