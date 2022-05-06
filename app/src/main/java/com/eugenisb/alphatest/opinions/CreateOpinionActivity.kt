package com.eugenisb.alphatest.opinions

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.profileAndHome.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_opinion.*

class CreateOpinionActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_opinion)

        title = "Create opinion"

        val movieName = intent.extras?.getString("movieName")
        val moviePoster = intent.extras?.getString("moviePoster")
        val userId = FirebaseAuth.getInstance().uid

        setMovie(movieName, moviePoster)

        ratingBar()

        cancelOpinionButton.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(profileIntent)
        }

        createOpinionButton.setOnClickListener {
            createOpinion(movieName, moviePoster, userId)
        }


    }

    private fun createOpinion(movieName: String?, moviePoster: String?, userId: String?) {

        val rating = opinionRatingBar.rating
        val opinionComment = opinionMultiline.text.toString()

        val opinion = hashMapOf(
            "creator" to userId,
            "movie" to movieName,
            "moviePoster" to moviePoster,
            "rating" to rating,
            "opinionComment" to opinionComment,
            "public" to publicOpinionCheckBox.isChecked
        )

        db.collection("opinions").add(opinion)


        val myOpinionsIntent = Intent(this, MyOpinionsActivity::class.java)
        myOpinionsIntent.flags = FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(myOpinionsIntent)
    }

    private fun ratingBar() {
        opinionRatingBar.rating = 5F
        ratingTextView.text = "5" + "/10"
        opinionRatingBar.setOnRatingBarChangeListener { ratingBar, rating, _ ->
            ratingTextView.text = "${rating.toInt()}/"+ "${ratingBar.numStars}"
        }
    }

    private fun setMovie(movieName: String?, moviePoster: String?) {

        movieNameOpiniontextView.text = movieName
        Picasso.get().load(moviePoster).into(opinionImageView)
    }


}