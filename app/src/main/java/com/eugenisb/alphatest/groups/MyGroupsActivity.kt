package com.eugenisb.alphatest.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_my_groups.*

class MyGroupsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_groups)

        title = "My groups"

        verifyUserLoggedIn()
        val userId = FirebaseAuth.getInstance().uid

        createGroupfloatingActionButton.setOnClickListener {
            val createGroupIntent = Intent(this,CreateGroupMembersActivity::class.java)
            createGroupIntent.putExtra("userId",userId)
            startActivity(createGroupIntent)
        }
    }

    private fun verifyUserLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intentAuth = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intentAuth)
        }
    }

}