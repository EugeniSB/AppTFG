package com.eugenisb.alphatest.opinions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.eugenisb.alphatest.auth.AuthActivity
import com.eugenisb.alphatest.profileAndHome.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_list.*

class MyOpinionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_opinions)

        title = "My opinions"

        val userId = FirebaseAuth.getInstance().uid

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mylists_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.create_list ->{
                val createOpIntent = Intent(this, SearchMovieAPIActivity::class.java)
                createOpIntent.putExtra("screen", "opinion")
                createOpIntent.putExtra("contactId", FirebaseAuth.getInstance().uid)
                startActivity(createOpIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}