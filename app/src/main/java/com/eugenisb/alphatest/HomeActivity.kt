package com.eugenisb.alphatest

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import android.graphics.Color.parseColor
import android.text.Html
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFFFF")))
        supportActionBar/* or getSupportActionBar() */!!.title = HtmlCompat.fromHtml("<font color=\"black\"> Home </font>", FROM_HTML_MODE_LEGACY);
        supportActionBar!!.elevation = 0f
        //title = "Home"

        val userId = verifyUserLoggedIn()

        contacts(userId)
        addContactsProvisonal(userId)

        val addContactsButton = findViewById<Button>(R.id.addContactsButton)
        addContactsButton.setOnClickListener {

            val addContactsIntent = Intent(this, AddContactActivity::class.java).apply {
                putExtra("userId", userId)

            }
            startActivity(addContactsIntent)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.home_profile->{
                val profileIntent = Intent(this, ProfileActivity::class.java)
                startActivity(profileIntent)
            }
            R.id.home_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intentAuth = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentAuth)
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun addContactsProvisonal(userId: String) {

        addContactsButton.setOnClickListener {
            val addContactsIntent = Intent(this, AddContactActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(addContactsIntent)
        }
    }
}