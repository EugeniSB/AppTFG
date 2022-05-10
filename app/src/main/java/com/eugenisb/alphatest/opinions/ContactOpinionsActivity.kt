package com.eugenisb.alphatest.opinions


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import com.eugenisb.alphatest.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_opinions.*
import kotlinx.android.synthetic.main.my_opinions_item.view.*
import kotlinx.android.synthetic.main.opinion_popup.view.*

class ContactOpinionsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var adapter : GroupAdapter<GroupieViewHolder>

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_opinions)

        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")

        title = contactUsername + " opinions"

        if(contactId != null)
            getOpinions(contactId)

    }

    inner class OpinionsItem(val movieName: String, val movieImgURL: String, val movieOpinion: String,
                             val movieRating: Int, val opinionId: String): Item<GroupieViewHolder>(){


        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myOpinionMovieTitle.text = movieName
            viewHolder.itemView.myOpinionText.text = movieOpinion
            viewHolder.itemView.myOpinionRating.text = "$movieRating/10"
            Picasso.get().load(movieImgURL).into(viewHolder.itemView.myOpinionImageView)


            viewHolder.itemView.setOnClickListener {

                layoutInf = applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.opinion_popup,null) as ViewGroup

                Picasso.get().load(movieImgURL).into(container.popUpOpinionImageView)
                container.movieNamePopUpTextView.text = movieName
                container.popUpRatingTextView.text =  "$movieRating /10"
                container.myOpinionPopUpText.text = movieOpinion
                container.myOpinionPopUpText.movementMethod = ScrollingMovementMethod()

                popUp = PopupWindow(container,925,1550,true)
                popUp.showAtLocation(myOpinionsLayout, Gravity.CENTER, 0,50)
                popUp.elevation = 100F
                myOpinionsRecyclerView.alpha = 0.5F
                myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myOpinionsRecyclerView.alpha = 1F
                    myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }
            }

        }

        override fun getLayout(): Int {
            return R.layout.contact_opinions_item
        }
    }

    private fun getOpinions(contactId: String) {

        db.collection("opinions").whereEqualTo("creator", contactId).get().addOnSuccessListener {
                results ->
            adapter = GroupAdapter<GroupieViewHolder>()

            for(document in results.documents){
                if(document["public"] as Boolean) {
                    val movieName = document["movie"] as String
                    val moviePoster = document["moviePoster"] as String
                    val movieOpinion = document["opinionComment"] as String
                    val movieRating = document["rating"] as Double

                    adapter.add(
                        OpinionsItem(movieName,moviePoster,
                            movieOpinion, movieRating.toInt(), document.id))
                }
            }

            myOpinionsRecyclerView.adapter = adapter
        }

    }
}