package com.eugenisb.alphatest.opinions

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.clases.Opinion
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_opinions.*
import kotlinx.android.synthetic.main.contact_opinions_item.view.*
import kotlinx.android.synthetic.main.my_opinions_item.view.*
import kotlinx.android.synthetic.main.my_opinions_item.view.myOpinionImageView
import kotlinx.android.synthetic.main.my_opinions_item.view.myOpinionMovieTitle
import kotlinx.android.synthetic.main.my_opinions_item.view.myOpinionRating
import kotlinx.android.synthetic.main.my_opinions_item.view.myOpinionText
import kotlinx.android.synthetic.main.opinion_popup.view.*
import java.io.Serializable
import java.util.*

class AllOpinionsOpinionActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    lateinit var adapter : GroupAdapter<GroupieViewHolder>
    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_opinions)

        val movieName = intent.extras?.getString("movieName")!!
        val moviePoster = intent.extras?.getString("moviePoster")
        val opinionsArray = intent.extras?.getSerializable("opinions") as ArrayList<Opinion>

        title = "Opinions of $movieName"

        printOpinions(movieName,opinionsArray)

    }

    inner class OpinionsItem(val movieName: String, val movieImgURL: String, val movieOpinion: String,
                             val movieRating: Int, val creator: String): Item<GroupieViewHolder>(){


        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myOpinionMovieTitle.text = movieName
            viewHolder.itemView.opinionCreatorTextView.visibility = View.VISIBLE
            viewHolder.itemView.opinionCreatorTextView.text = "By: $creator"

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

    private fun printOpinions(movieName : String, opinionList: ArrayList<Opinion>) {

        adapter = GroupAdapter<GroupieViewHolder>()

        for(opinion in opinionList){
            adapter.add(
                OpinionsItem(movieName,opinion.moviePoster,
                    opinion.opinionComment, opinion.rating, opinion.creator))

        }

        myOpinionsRecyclerView.adapter = adapter
    }


}