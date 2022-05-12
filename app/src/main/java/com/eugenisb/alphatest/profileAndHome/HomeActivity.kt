package com.eugenisb.alphatest.profileAndHome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.FragmentPagerAdapter
import com.eugenisb.alphatest.*
import com.eugenisb.alphatest.adapters.ViewPagerAdapter
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.contacts.ContactsFragment
import com.eugenisb.alphatest.groups.GroupsFragment
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFFFF")))
        //supportActionBar/* or getSupportActionBar() */!!.title = HtmlCompat.fromHtml("<font color=\"black\"> Home </font>", FROM_HTML_MODE_LEGACY);
        //supportActionBar!!.elevation = 0f
        title = "Home"

        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        if(userId != null){
            //contacts(userId)
            //groups(userId)
        }

        supportActionBar!!.elevation = 0F

        setUpTabs()

        /*
        homerecommendButton.setOnClickListener {
            val profileIntent = Intent(this, FragmentActivity::class.java)
            startActivity(profileIntent)
        }

         */


    }

    private fun setUpTabs() {

        tabLayout.setupWithViewPager(viewPager)

        var vp = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )

        vp.addFragment(ContactsFragment(), "Contacts")
        vp.addFragment(GroupsFragment(), "Groups")
        viewPager.adapter = vp

        tabLayout.setupWithViewPager(viewPager)

    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.home_profile ->{
                val profileIntent = Intent(this, ProfileActivity::class.java)
                startActivity(profileIntent)
            }
            R.id.home_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intentAuth = Intent(this, AuthActivity::class.java)
                intentAuth.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intentAuth)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*
    private fun  contacts(userId: String){

        val contactsButton = findViewById<Button>(R.id.mycontactsButton)

        contactsButton.setOnClickListener {
            val contactsIntent = Intent(this, MyContactsActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(contactsIntent)
        }
    }

    private fun  groups(userId: String){

        mygroupsButton.setOnClickListener {
            val groupsIntent = Intent(this, MyGroupsActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(groupsIntent)
        }
    }

     */

}