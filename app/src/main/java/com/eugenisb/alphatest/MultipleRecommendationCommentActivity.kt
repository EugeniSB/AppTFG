package com.eugenisb.alphatest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.profileAndHome.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_contact_recommendation.*

class MultipleRecommendationCommentActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_recommendation)

        title = "Recommendation comment"

        val movieName = intent.extras?.getString("movieName")
        val moviePoster = intent.extras?.getString("moviePoster")
        var usersToRecommend = intent.extras?.getSerializable("usersGroupMap") as Map<String, String>

        movieTitleTextView.text = movieName

        finishContactRecommendationButton.setOnClickListener {
            if(movieName != null && moviePoster != null && usersToRecommend.isNotEmpty()) {
                createRecommendation(movieName,moviePoster, usersToRecommend)
            }
        }
    }

    private fun createRecommendation(movieName: String, moviePoster: String, usersToRecommend: Map<String, String>) {

        val movieComment = movieCommentMultiline.text.toString()
        val userId = FirebaseAuth.getInstance().uid!!

        if(movieComment.isNotEmpty()){

            for(id in usersToRecommend.keys){

                val time = java.sql.Timestamp(System.currentTimeMillis())

                val recommendation = hashMapOf(
                    "name" to movieName,
                    "comment" to movieComment,
                    "poster" to moviePoster,
                    "from" to userId,
                    "to" to id,
                    "Date" to time
                )

                db.collection("user-recommendations").document(userId).collection(id).add(recommendation).addOnSuccessListener {
                    db.collection("user-recommendations").document(id).collection(userId).document(it.id).set(recommendation)
                }


            }

            val homeIntent = Intent(this, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(homeIntent)
        }
    }
}