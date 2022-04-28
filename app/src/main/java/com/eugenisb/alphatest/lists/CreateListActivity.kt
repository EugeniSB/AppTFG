package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_list.*

class CreateListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_list)


        title = "Create list"
        val userId = FirebaseAuth.getInstance().uid

        nextListbutton.setOnClickListener {

            if(listNameEditText.text.isNotEmpty()){
                val searchListIntent = Intent(this, SearchMovieAPIActivity::class.java)
                searchListIntent.putExtra("screen", "createList")
                searchListIntent.putExtra("contactUsername", listNameEditText.text.toString())
                searchListIntent.putExtra("contactId", userId)
                searchListIntent.putExtra("isChecked", publicListcheckBox.isChecked )

                startActivity(searchListIntent)
            }

        }


    }

}