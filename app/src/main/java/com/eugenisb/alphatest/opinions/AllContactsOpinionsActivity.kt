package com.eugenisb.alphatest.opinions

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.lifecycle.lifecycleScope
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class AllContactsOpinionsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    lateinit var adapter : GroupAdapter<GroupieViewHolder>
    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_opinions)

        title = "Contacts Opinions"

        val userId = FirebaseAuth.getInstance().uid!!

        noAllOpinionsTextView.visibility = VISIBLE

        lifecycleScope.launch {
            val map = getOpinions(userId)
            printOpinions(map.toSortedMap())
        }
    }


    private suspend fun getUserSnapshot(userId: String): DocumentSnapshot {
        return db.collection("users").document(userId).get().await()
    }

    private suspend fun getOpinionsSnapshot(contactId: String): QuerySnapshot {
        return db.collection("opinions").whereEqualTo("creator", contactId).get().await()
    }

    private suspend fun getContactsNames(contactsIds: ArrayList<String>): Map<String,String> {

        val contactsMap = mutableMapOf<String,String>()

        for(id in contactsIds) {
            val userDoc = getUserSnapshot(id)
            contactsMap[id] = userDoc["username"] as String
        }

        return contactsMap
    }

    private suspend fun getOpinions(userId: String): MutableMap<String, ArrayList<Opinion>> {
        val userDoc = getUserSnapshot(userId)
        val contactsIds = userDoc["contacts"] as ArrayList<String>
        val contactsMap = getContactsNames(contactsIds)
        val opinionsMap = mutableMapOf<String,ArrayList<Opinion>>()

        if(contactsIds.isNotEmpty()) {
            for (id in contactsIds) {
                val opinionsDocs = getOpinionsSnapshot(id)
                    for (document in opinionsDocs.documents) {
                        val isPublic = document["public"] as Boolean
                        if (isPublic) {
                            noAllOpinionsTextView.visibility = GONE
                            val rating = document["rating"] as Double
                            val opinion = Opinion(
                                contactsMap[id]!!,
                                document["moviePoster"] as String,
                                document["opinionComment"] as String,
                                document["public"] as Boolean,
                                rating.toInt()
                            )
                            if (opinionsMap[document["movie"] as String].isNullOrEmpty())
                                opinionsMap[document["movie"] as String] = ArrayList<Opinion>()
                            opinionsMap[document["movie"] as String]?.add(opinion)
                        }
                    }
            }
        }else{
            noContactsAllOpinions.visibility = VISIBLE
            noAllOpinionsTextView.visibility = GONE
        }
        return opinionsMap
    }



    inner class OpinionsItem(val movieName: String, val movieImgURL: String, val movieOpinion: String,
                             val movieRating: Int, val creator: String): Item<GroupieViewHolder>(){


        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myOpinionMovieTitle.text = movieName
            viewHolder.itemView.opinionCreatorTextView.visibility = VISIBLE
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

    private fun printOpinions(opinionsMap: SortedMap<String, ArrayList<Opinion>>) {

        adapter = GroupAdapter<GroupieViewHolder>()

        for(movie in opinionsMap.keys){
            for(opinion in opinionsMap[movie]!!){
                adapter.add(
                    OpinionsItem(movie,opinion.moviePoster,
                        opinion.opinionComment, opinion.rating, opinion.creator))
            }

        }

        myOpinionsRecyclerView.adapter = adapter
    }

}





class Opinion (val creator: String, val moviePoster: String, val opinionComment: String,
               val public: Boolean, val rating: Int){
    constructor() : this("","","",false,0)

}

