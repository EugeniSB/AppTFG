package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_chat_log.*
import kotlinx.android.synthetic.main.activity_contact_recommendation.*
import java.text.SimpleDateFormat
import java.util.*


class ContactRecommendationActivity : AppCompatActivity() {

    private val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_recommendation)

        val movieName = intent.extras?.getString("movieName")
        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")
        val moviePoster = intent.extras?.getString("moviePoster")

        movieNameEditText.text = movieName

        title = "Recommend to $contactUsername"

        finishContactRecommendationButton.setOnClickListener {
            if(contactId != null && contactUsername != null && moviePoster != null && movieName != null) {
                createRecommendation(contactId,contactUsername, moviePoster, movieName)
            }
        }
    }

    private fun createRecommendation(contactId: String, contactUsername: String, moviePoster: String,
        movieName: String) {

        val movieComment = movieCommentMultiline.text.toString()
        val userId = FirebaseAuth.getInstance().uid!!

        if(movieComment.isNotEmpty()){

            val time = java.sql.Timestamp(System.currentTimeMillis())
            //val time2 = Date()
            //val time2.0 = sdf.format(time)
            //val time3.0 = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time2)


            val recommendation = hashMapOf(
                "name" to movieName,
                "comment" to movieComment,
                "poster" to moviePoster,
                "from" to userId,
                "to" to contactId,
                "Date" to time
            )

            //db.collection("user-recommendations").document(userId).collection(contactId).add(recommendation)
            //db.collection("user-recommendations").document(contactId).collection(userId).add(recommendation)

            db.collection("user-recommendations").document(userId).collection(contactId).add(recommendation).addOnSuccessListener {
                db.collection("user-recommendations").document(contactId).collection(userId).document(it.id).set(recommendation)
            }

            val chatIntent = Intent(this, ContactChatLogActivity::class.java)
            //chatIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            chatIntent.putExtra("contactId", contactId)
            chatIntent.putExtra("contactUsername", contactUsername)
            startActivity(chatIntent)


            //onBackPressed()

        }

    }

}