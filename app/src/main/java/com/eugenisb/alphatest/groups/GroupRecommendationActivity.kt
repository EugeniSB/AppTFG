package com.eugenisb.alphatest.groups

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_recommendation.*
import kotlinx.android.synthetic.main.activity_group_recommendation.*

class GroupRecommendationActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_recommendation)

        val groupId = intent.extras?.getString("groupId")
        val groupName =  intent.extras?.getString("groupName")

        title = "Recommend Group"

        finishGroupRecommendationButton.setOnClickListener {
            if(groupId != null && groupName != null) {
                createRecommendation(groupId,groupName)
            }
        }

    }

    private fun createRecommendation(groupId: String, groupName: String) {

        val movieName = movieNameGroupEditText.text.toString()
        val movieComment = movieCommentGroupMultiline.text.toString()
        val userId = FirebaseAuth.getInstance().uid!!

        if(movieName.isNotEmpty() && movieComment.isNotEmpty()){

            val time = java.sql.Timestamp(System.currentTimeMillis())

            val recommendation = hashMapOf(
                "name" to movieName,
                "comment" to movieComment,
                "from" to userId,
                "to" to groupId,
                "itsGroup" to true,
                "Date" to time
            )

            //db.collection("user-recommendations").document(userId).collection(groupId).add(recommendation)
            db.collection("group-recommendations").document(groupId).collection(groupId).add(recommendation)

            onBackPressed()
        }


    }

}