package com.eugenisb.alphatest.profileAndHome

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    lateinit var URIimage : Uri
    private var imageUpdated : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)


        title = "EditProfile"

        val bundle = intent.extras
        //val userId = bundle?.getString("userId")
        val userId = verifyUserLoggedIn()
        getUser(userId)
        var username = ""
        var email = ""

        lifecycleScope.launch {
            username = getUsername(userId ?: "")
            email = getEmail(userId ?: "")
        }

        val cancelButton = findViewById<Button>(R.id.cancelEditProfileButton)
        cancelButton.setOnClickListener {
            onBackPressed()
        }

        val editButton = findViewById<Button>(R.id.acceptEditProfileButton)
        editButton.setOnClickListener {
            editUser(userId ?: "", username, email)
        }

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                findViewById<ImageView>(R.id.circleAvatarEditProfile).setImageURI(it)
                URIimage = it
                imageUpdated = true

            }
        )

        val uploadImage = findViewById<Button>(R.id.uploadImageProfileButton)
        uploadImage.setOnClickListener {
            getImage.launch("image/*")
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

    private fun getUser(userId: String) {

        db.collection("users").document(userId).get().addOnSuccessListener {
            findViewById<EditText>(R.id.fullnameEditProfileEditText).setText(it.get("name") as String)
            findViewById<EditText>(R.id.usernameEditProfileEditText).setText(it.get("username") as String)
            //findViewById<EditText>(R.id.phoneEditProfileEditText).setText(it.get("phone") as String)
        }

        val imgReference = storage.getReference().child("images/profile_pics/Profile_picture_of: " + userId)

        val localFile = File.createTempFile("profile_pic", userId)

        imgReference.getFile(localFile).addOnSuccessListener {
            //Toast.makeText(this, "Profile Image Retrieved", Toast.LENGTH_SHORT).show()
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            findViewById<ImageView>(R.id.circleAvatarEditProfile).setImageBitmap(bitmap)

        }/*.addOnFailureListener {
            Toast.makeText(this, "Error Retrieving Profile Image", Toast.LENGTH_SHORT).show()
        }*/

    }

    private suspend fun getUsernameSnapshot(userId: String): DocumentSnapshot{
        return db.collection("users").document(userId).get().await()
    }

    private suspend fun getUsername(userId: String): String{
        val userDoc = getUsernameSnapshot(userId)
        return userDoc.getString("username")!!
    }

    private suspend fun getEmail(userId: String): String{
        var snap = db.collection("users").document(userId).get().await()
        return snap.getString("email")!!
    }


    private fun editUser(userId: String, username: String,email: String){

        if(validateFullName() && validateUsername() /*&& validatePhoneNumber()*/) {

            val name = findViewById<EditText>(R.id.fullnameEditProfileEditText).text.toString()
            val newUsername = findViewById<EditText>(R.id.usernameEditProfileEditText).text.toString()

            //val phone = findViewById<EditText>(R.id.phoneEditProfileEditText).text.toString()

            db.collection("users").whereEqualTo("username", newUsername).get().addOnSuccessListener{
                if(it.isEmpty || username == newUsername){
                    db.collection("users").document(userId).update("name",name)
                    db.collection("users").document(userId).update("username",newUsername)

                    if(imageUpdated){
                        val imageName = "Profile_picture_of: " + userId
                        val storageReference = storage.getReference("images/profile_pics/$imageName")

                        storageReference.putFile(URIimage).addOnSuccessListener {
                            Toast.makeText(this, "Successfuly uploaded image", Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener {
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val profileIntent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("userId", userId)
                    }
                    startActivity(profileIntent)
                }else{
                    val alertUsernameAlreadyExists = AlertDialog.Builder(this)
                    alertUsernameAlreadyExists.setTitle("Username error")
                    alertUsernameAlreadyExists.setMessage("Username already exists")
                    alertUsernameAlreadyExists.setPositiveButton("Okay", null)
                    alertUsernameAlreadyExists.show()
                }
            }

        }
    }

    private fun validateFullName() : Boolean {
        val fullName = findViewById<EditText>(R.id.fullnameEditProfileEditText).text.toString()

        if(fullName.isNotEmpty()){
            return true
        }else{
            val alertFullname = AlertDialog.Builder(this)
            alertFullname.setTitle("Error full name")
            alertFullname.setMessage("Full name can not be empty, please try again")
            alertFullname.setPositiveButton("Okay", null)
            alertFullname.show()
            return false
        }
    }

    private fun validateUsername(): Boolean{
        val username = findViewById<EditText>(R.id.usernameEditProfileEditText).text.toString()

        if(username.isNotEmpty()){
            return true
        }else{
            val alertFullname = AlertDialog.Builder(this)
            alertFullname.setTitle("Error username")
            alertFullname.setMessage("Username can not be empty, please try again")
            alertFullname.setPositiveButton("Okay", null)
            alertFullname.show()
            return false
        }
    }


    private fun validatePhoneNumber() : Boolean{
        /*val phoneNum = findViewById<EditText>(R.id.phoneEditProfileEditText).text.toString()
        if(phoneNum.isNotEmpty() && Patterns.PHONE.matcher(phoneNum).matches())
            return true
        else{
            val alertPhoneNum = AlertDialog.Builder(this)
            alertPhoneNum.setTitle("Error phone number")
            alertPhoneNum.setMessage("Incorrect phone number, please try again")
            alertPhoneNum.setPositiveButton("Okay", null)
            alertPhoneNum.show()
            return false
        }*/
        return true
    }

}