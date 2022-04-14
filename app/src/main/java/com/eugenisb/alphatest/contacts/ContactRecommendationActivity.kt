package com.eugenisb.alphatest.contacts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_recommendation.*
import java.text.SimpleDateFormat
import java.util.*


class ContactRecommendationActivity : AppCompatActivity() {

    private val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_recommendation)

        title = "Recommend Contact"
        val contactId = intent.extras?.getString("contactId")

        finishContactRecommendationButton.setOnClickListener {
            if(contactId != null) {
                createRecommendation(contactId)
            }
        }
    }

    private fun createRecommendation(contactId: String) {

        val movieName = movieNameEditText.text.toString()
        val movieComment = movieCommentMultiline.text.toString()
        val userId = FirebaseAuth.getInstance().uid!!

        if(movieName.isNotEmpty() && movieComment.isNotEmpty()){

            val time = java.sql.Timestamp(System.currentTimeMillis())
            //val time2 = Date()
            //val time2.0 = sdf.format(time)
            //val time3.0 = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time2)


            val recommendation = hashMapOf(
                "name" to movieName,
                "comment" to movieComment,
                "from" to userId,
                "to" to contactId,
                "itsGroup" to false,
                "Date" to time
            )

            db.collection("recommendations").add(recommendation)

        }

    }

}