package com.eugenisb.alphatest

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_profile.*
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storageReference = FirebaseStorage.getInstance().getReference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = "Profile"

        val bundle = intent.extras
        val userId = verifyUserLoggedIn()

        getUser(userId ?: "")

        val backArrow = findViewById<ImageView>(R.id.backArrowProfile)

        backArrow.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(homeIntent)
            //onBackPressed()
        }

        val editProfile = findViewById<ImageView>(R.id.editprofileIcon)
        editProfile.setOnClickListener {
            editProfile(userId ?: "")
        }

    }

    private fun verifyUserLoggedIn() : String{
        val uid = FirebaseAuth.getInstance().uid ?: ""
        if(uid == null){
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
        return uid
    }

    private fun getUser(userId: String){

        db.collection("users").document(userId).get().addOnSuccessListener {
            findViewById<TextView>(R.id.fullnameTextView).setText(it.get("name") as String)
            findViewById<TextView>(R.id.usernameTextView).setText(it.get("username") as String)
            findViewById<TextView>(R.id.emailTextView).setText(it.get("email") as String)
            //findViewById<TextView>(R.id.phoneTextView).setText(it.get("phone") as String)
        }

        Thread.sleep(1100)

        val imgReference = storageReference.child("images/profile_pics/Profile_picture_of: " + userId)

        val localFile = File.createTempFile("profile_pic", userId)

        imgReference.getFile(localFile).addOnSuccessListener {
            //Toast.makeText(this, "Profile Image Retrieved", Toast.LENGTH_SHORT).show()
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            findViewById<ImageView>(R.id.circleProfileImage).setImageBitmap(bitmap)
            profileImage.alpha = 0f

        }/*.addOnFailureListener {
            Toast.makeText(this, "Error Retrieving Profile Image", Toast.LENGTH_SHORT).show()
        }*/

    }

    private fun editProfile(userId: String) {

        val editProfileIntent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("userId", userId)
        }
        startActivity(editProfileIntent)
    }
}