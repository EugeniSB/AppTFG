package com.eugenisb.alphatest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        title = "Home"

        val userId = verifyUserLoggedIn()

        logoff()
        profile(userId ?: "")
        contacts(userId ?: "")

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

    private fun profile(userId: String){

        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        profileIcon.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(profileIntent)
        }
    }

    private fun  contacts(userId: String){

        val contactsButton = findViewById<Button>(R.id.mycontactsButton)

        contactsButton.setOnClickListener {
            val contactsIntent = Intent(this, MyContactsActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(contactsIntent)
        }
    }

    private fun logoff(){

        val logoffButton = findViewById<Button>(R.id.logoffButton)

        logoffButton.setOnClickListener{
            val logoffIntent = Intent(this, AuthActivity::class.java)
            startActivity(logoffIntent)
            FirebaseAuth.getInstance().signOut()
            //onBackPressed()
        }
    }
}