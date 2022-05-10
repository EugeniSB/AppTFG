package com.eugenisb.alphatest.groups

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.contacts.ContactChatLogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_recommendation.*
import kotlinx.android.synthetic.main.activity_group_recommendation.*

class GroupRecommendationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_recommendation)

        val movieName = intent.extras?.getString("movieName")
        val groupId = intent.extras?.getString("groupId")
        val groupName =  intent.extras?.getString("contactUsername")
        val moviePoster = intent.extras?.getString("moviePoster")

        title = "Recommend Group"

        movieNameGroupEditText.setText(movieName)

        finishGroupRecommendationButton.setOnClickListener {
            if(groupId != null && groupName != null && moviePoster != null) {
                createRecommendation(groupId,groupName,moviePoster)
            }
        }

    }

    private fun createRecommendation(groupId: String, groupName: String, moviePoster: String) {

        val movieName = movieNameGroupEditText.text.toString()
        val movieComment = movieCommentGroupMultiline.text.toString()
        val userId = FirebaseAuth.getInstance().uid!!

        if(movieName.isNotEmpty() && movieComment.isNotEmpty()){

            val time = java.sql.Timestamp(System.currentTimeMillis())

            val recommendation = hashMapOf(
                "name" to movieName,
                "comment" to movieComment,
                "poster" to moviePoster,
                "from" to userId,
                "to" to groupId,
                "Date" to time
            )

            //db.collection("user-recommendations").document(userId).collection(groupId).add(recommendation)
            db.collection("group-recommendations").document(groupId).collection(groupId).add(recommendation)

            val chatIntent = Intent(this, GroupChatLogActivity::class.java)
            //chatIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            chatIntent.putExtra("groupId", groupId)
            chatIntent.putExtra("groupName", groupName)
            startActivity(chatIntent)

            //onBackPressed()
        }


    }

}