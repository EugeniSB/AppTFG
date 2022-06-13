package com.eugenisb.alphatest.opinions

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.View.VISIBLE
import android.widget.PopupWindow
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_lists.*
import kotlinx.android.synthetic.main.activity_my_opinions.*
import kotlinx.android.synthetic.main.my_opinions_item.view.*
import kotlinx.android.synthetic.main.opinion_popup.view.*

class MyOpinionsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var adapter : GroupAdapter<GroupieViewHolder>

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_opinions)

        title = "My opinions"

        val userId = FirebaseAuth.getInstance().uid

        getOpinions(userId)

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mylists_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.create_list ->{
                val createOpIntent = Intent(this, SearchMovieAPIActivity::class.java)
                createOpIntent.putExtra("screen", "opinion")
                createOpIntent.putExtra("contactId", FirebaseAuth.getInstance().uid)
                startActivity(createOpIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class OpinionsItem(val movieName: String, val movieImgURL: String, val movieOpinion: String,
                             val movieRating: Int, val opinionId: String): Item<GroupieViewHolder>(){


        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myOpinionMovieTitle.text = movieName
            viewHolder.itemView.myOpinionMovieTitle.paintFlags =
                viewHolder.itemView.myOpinionMovieTitle.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            viewHolder.itemView.myOpinionText.text = movieOpinion
            viewHolder.itemView.myOpinionRating.text = "$movieRating/10"
            Picasso.get().load(movieImgURL).into(viewHolder.itemView.myOpinionImageView)

            viewHolder.itemView.deleteOpinionImageButton.setOnClickListener {

                ////POSAR AVIS DE QUE ES BORRARA LA LLISTA
                db.collection("opinions").document(opinionId).delete()
                adapter.removeGroupAtAdapterPosition(position)
                if(adapter.itemCount == 0){
                    noOpinionsTextView.visibility = VISIBLE
                }

            }

            viewHolder.itemView.editOpinionImageButton.setOnClickListener {
                val editOpinionItemIntent = Intent(viewHolder.itemView.myOpinionImageView.context, EditOpinionActivity::class.java)
                editOpinionItemIntent.putExtra("opinionId", opinionId)
                viewHolder.itemView.myOpinionImageView.context.startActivity(editOpinionItemIntent)
                adapter.notifyItemRemoved(position)
                adapter.notifyDataSetChanged()

            }

            viewHolder.itemView.setOnClickListener {

                layoutInf = applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.opinion_popup,null) as ViewGroup

                Picasso.get().load(movieImgURL).into(container.popUpOpinionImageView)
                container.movieNamePopUpTextView.text = movieName
                container.movieNamePopUpTextView.paintFlags =
                    container.movieNamePopUpTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                container.popUpRatingTextView.text =  "$movieRating /10"
                container.myOpinionPopUpText.text = movieOpinion
                container.myOpinionPopUpText.movementMethod = ScrollingMovementMethod()

                popUp = PopupWindow(container,925,1550,true)
                popUp.showAtLocation(myOpinionsLayout, Gravity.CENTER, 0,50)
                popUp.elevation = 100F
                myOpinionsRecyclerView.alpha = 0.5F //TODO AQUI PROVAR DE POSAR EL LAYOUT
                myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myOpinionsRecyclerView.alpha = 1F
                    myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }
            }

        }

        override fun getLayout(): Int {
            return R.layout.my_opinions_item
        }
    }

    private fun getOpinions(userId: String?) {

        db.collection("opinions").whereEqualTo("creator", userId).get().addOnSuccessListener {
                results ->
            adapter = GroupAdapter<GroupieViewHolder>()

            if(results.documents.isNotEmpty()){
                for(document in results.documents){

                    val movieName = document["movie"] as String
                    val moviePoster = document ["moviePoster"] as String
                    val movieOpinion = document["opinionComment"] as String
                    val movieRating = document["rating"] as Double

                    adapter.add(OpinionsItem(movieName,moviePoster,movieOpinion,movieRating.toInt(),document.id))
                }

                /*adapter.setOnItemClickListener{ item,view ->
                    val contactItem = item as MyContactsActivity.ContactItem

                    val intentChatLog = Intent(view.context, ContactChatLogActivity::class.java)
                    intentChatLog.putExtra("contactId",item.userId)
                    intentChatLog.putExtra("contactUsername",item.username)
                    startActivity(intentChatLog)


                }
                */
                myOpinionsRecyclerView.adapter = adapter
            }else{
                noOpinionsTextView.visibility = VISIBLE
            }
        }

    }

}