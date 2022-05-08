package com.eugenisb.alphatest.contacts

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.lists.ContactListsActivity
import com.eugenisb.alphatest.opinions.ContactOpinionsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_contact_profile.*
import java.io.File

class ContactProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().getReference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_profile)

        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")

        title = "Profile of $contactUsername"

        if (contactId != null){
            getUser(contactId)
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
            contactFullNameTextView.text = it.get("name") as String
            contactUsernameTextView.text = it.get("username") as String
            contactEmailTextView.text = it.get("email") as String
        }

        val imgReference = storageReference.child("images/profile_pics/Profile_picture_of: " + contactId)

        val localFile = File.createTempFile("profile_pic", contactId)

        imgReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            contactCircleProfileImage.setImageBitmap(bitmap)
            contactProfileImage.alpha = 0f
        }

    }
}