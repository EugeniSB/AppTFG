package com.eugenisb.alphatest.contacts

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.groups.GroupChatLogActivity
import com.eugenisb.alphatest.lists.ContactListsActivity
import com.eugenisb.alphatest.opinions.ContactOpinionsActivity
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_contact_profile.*
import java.io.File

class ContactProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().getReference()
    private val contactUsername : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_profile)

        val contactId = intent.extras?.getString("contactId")


        if (contactId != null){
            getUser(contactId)
        }

        deleteContactButton.setOnClickListener {
            if(contactId != null)
                deleteContact(contactId)
        }

        contactListsButton.setOnClickListener {
            if (contactId != null){
                val contactListsIntent = Intent(this, ContactListsActivity::class.java)
                contactListsIntent.putExtra("contactId", contactId)
                contactListsIntent.putExtra("contactUsername", contactUsername)
                startActivity(contactListsIntent)
            }
        }

        contactOpinionsButton.setOnClickListener {
            if (contactId != null){
                val contactOpinionsIntent = Intent(this, ContactOpinionsActivity::class.java)
                contactOpinionsIntent.putExtra("contactId", contactId)
                contactOpinionsIntent.putExtra("contactUsername", contactUsername)
                startActivity(contactOpinionsIntent)
            }
        }


    }

    private fun getUser(contactId: String) {

        db.collection("users").document(contactId).get().addOnSuccessListener {
            val contactUsername = it.get("username") as String
            contactFullNameTextView.text = contactUsername
            contactUsernameTextView.text = it.get("name") as String
            title = "Profile of $contactUsername"
            contactEmailTextView.text = "Email: " + it.get("email") as String
        }

        val imgReference = storageReference.child("images/profile_pics/Profile_picture_of: " + contactId)

        val localFile = File.createTempFile("profile_pic", contactId)

        imgReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            contactCircleProfileImage.setImageBitmap(bitmap)
            contactProfileImage.alpha = 0f
        }

    }

    private fun deleteContact(contactId: String){

        val userId = FirebaseAuth.getInstance().uid

        if(userId != null) {
            db.collection("users").document(contactId)
                .update("contacts", FieldValue.arrayRemove(userId))
            db.collection("users").document(userId)
                .update("contacts", FieldValue.arrayRemove(contactId))

            val homeIntent = Intent(this, HomeActivity::class.java)
            //chatIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeIntent)
        }
    }
}