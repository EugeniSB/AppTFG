package com.eugenisb.alphatest.opinions

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.profileAndHome.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_opinion.*
import kotlinx.android.synthetic.main.activity_edit_my_list.*
import kotlinx.android.synthetic.main.activity_edit_opinion.*

class EditOpinionActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_opinion)

        title = "Edit opinion"

        val userId = FirebaseAuth.getInstance().uid
        val opinionId = intent.extras?.getString("opinionId")

        if(opinionId != null)
            getOpinion(opinionId)

        cancelEditOpinionButton.setOnClickListener {
            val myOpinionsIntent = Intent(this, MyOpinionsActivity::class.java)
            myOpinionsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(myOpinionsIntent)
        }

        editOpinionButton.setOnClickListener {
            if(userId != null && opinionId != null)
                editOpinion(userId,opinionId)
        }

    }

    private fun editOpinion(userId: String, opinionId: String) {

        db.collection("opinions").document(opinionId).update("rating", editOpinionRatingBar.rating)
        db.collection("opinions").document(opinionId).update("opinionComment", editOpinionMultiline.text.toString())
        db.collection("opinions").document(opinionId).update("public", publicEditOpinionCheckBox.isChecked)

        val myOpinionsIntent = Intent(this, MyOpinionsActivity::class.java)
        Thread.sleep(800)
        startActivity(myOpinionsIntent)
        finish()
    }

    private fun getOpinion(opinionId: String) {
        db.collection("opinions").document(opinionId).get().addOnSuccessListener {

            Picasso.get().load(it["moviePoster" ] as String).into(
                editOpinionImageView)
            movieNameEditOpinionTextView.text = it["movie"] as String
            editOpinionRatingBar.rating = (it["rating"] as Double).toFloat()
            editRatingTextView.text = editOpinionRatingBar.rating.toInt().toString() + "/10"
            editOpinionMultiline.setText(it["opinionComment"] as String)
            publicEditOpinionCheckBox.isChecked = it["public"] as Boolean
        }

        editOpinionRatingBar.setOnRatingBarChangeListener { ratingBar, rating, _ ->
            editRatingTextView.text = "${rating.toInt()}/"+ "${ratingBar.numStars}"
        }
    }
}