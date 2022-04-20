package com.eugenisb.alphatest.contacts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")

        title = "Recommend to $contactUsername"

        finishContactRecommendationButton.setOnClickListener {
            if(contactId != null && contactUsername != null) {
                createRecommendation(contactId,contactUsername)
            }
        }
    }

    private fun createRecommendation(contactId: String, contactUsername: String) {

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

            db.collection("user-recommendations").document(userId).collection(contactId).add(recommendation)
            db.collection("user-recommendations").document(contactId).collection(userId).add(recommendation)

            /*
            val chatIntent = Intent(this, ContactChatLogActivity::class.java)
            chatIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            chatIntent.putExtra("contactId", contactId)
            chatIntent.putExtra("contactUsername", contactUsername)
            startActivity(chatIntent)

             */
            onBackPressed()

        }

    }

}