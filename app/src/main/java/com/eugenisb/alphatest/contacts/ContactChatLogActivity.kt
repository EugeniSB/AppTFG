package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.profileAndHome.ProfileActivity
import com.google.firebase.auth.FirebaseAuth

class ContactChatLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chat_log)

        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")


        title = contactUsername




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")
        when (item.itemId){
            R.id.contact_profile ->{
                val contactProfileIntent = Intent(this, ContactProfileActivity::class.java)
                contactProfileIntent.putExtra("contactId", contactId)
                contactProfileIntent.putExtra("contactUsername", contactUsername)
                startActivity(contactProfileIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}