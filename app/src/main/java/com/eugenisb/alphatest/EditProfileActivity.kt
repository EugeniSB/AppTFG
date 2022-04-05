package com.eugenisb.alphatest

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URI

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
        val email = bundle?.getString("email")

        getUser(email ?: "")

        val cancelButton = findViewById<Button>(R.id.cancelEditProfileButton)
        cancelButton.setOnClickListener {
            onBackPressed()
        }

        val editButton = findViewById<Button>(R.id.acceptEditProfileButton)
        editButton.setOnClickListener {
            editUser(email ?: "")
        }

        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                findViewById<ImageView>(R.id.editprofileAvatar).setImageURI(it)
                URIimage = it
                imageUpdated = true

                //val imageName = "Profile_picture_of: " + email
                //val storageReference = storage.getReference("images/profile_pics/$imageName")
                /*
                storageReference.putFile(it).addOnSuccessListener {
                    Toast.makeText(this, "Successfuly uploaded image",Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(this, "Upload failed",Toast.LENGTH_SHORT).show()
                }*/
            }
        )

        val uploadImage = findViewById<Button>(R.id.uploadImageProfileButton)
        uploadImage.setOnClickListener {
            getImage.launch("image/*")
        }


    }


    private fun getUser(email: String){

        db.collection("users").document(email).get().addOnSuccessListener {
            findViewById<EditText>(R.id.fullnameEditProfileEditText).setText(it.get("name") as String)
            findViewById<EditText>(R.id.usernameEditProfileEditText).setText(it.get("username") as String)
            //findViewById<EditText>(R.id.phoneEditProfileEditText).setText(it.get("phone") as String)
        }

        val imgReference = storage.getReference().child("images/profile_pics/Profile_picture_of: " + email)

        val localFile = File.createTempFile("profile_pic", email)

        imgReference.getFile(localFile).addOnSuccessListener {
            //Toast.makeText(this, "Profile Image Retrieved", Toast.LENGTH_SHORT).show()
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            findViewById<ImageView>(R.id.editprofileAvatar).setImageBitmap(bitmap)

        }/*.addOnFailureListener {
            Toast.makeText(this, "Error Retrieving Profile Image", Toast.LENGTH_SHORT).show()
        }*/

    }

    private fun editUser(email: String){

        if(validateFullName() && validateUsername() /*&& validatePhoneNumber()*/) {

            val name = findViewById<EditText>(R.id.fullnameEditProfileEditText).text.toString()
            val username = findViewById<EditText>(R.id.usernameEditProfileEditText).text.toString()
            //val phone = findViewById<EditText>(R.id.phoneEditProfileEditText).text.toString()

            db.collection("users").document(email).set(
                hashMapOf("name" to name, "username" to username/*, "phone" to phone*/)
            )

            if(imageUpdated){
                val imageName = "Profile_picture_of: " + email
                val storageReference = storage.getReference("images/profile_pics/$imageName")

                storageReference.putFile(URIimage).addOnSuccessListener {
                    Toast.makeText(this, "Successfuly uploaded image", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            val profileIntent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("email", email)
            }
            startActivity(profileIntent)
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