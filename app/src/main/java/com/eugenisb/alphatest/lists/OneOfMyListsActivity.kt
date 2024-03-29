package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_contact_chat_log.*
import kotlinx.android.synthetic.main.activity_my_lists.*
import kotlinx.android.synthetic.main.activity_one_of_my_lists.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_popup.view.*
import kotlinx.android.synthetic.main.one_of_my_lists_item.view.*

class OneOfMyListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var adapter : GroupAdapter<GroupieViewHolder>

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_of_my_lists)

        val listName = intent.extras?.getString("listName")
        val listId = intent.extras?.getString("listId")

        title = listName

        if(listId != null) {
            Thread.sleep(1000)
            getListItems(listId)
        }

        addToListFloatingActionButton.setOnClickListener {
            val addToListIntent = Intent(this, SearchMovieAPIActivity::class.java)
            addToListIntent.putExtra("screen", "list")
            addToListIntent.putExtra("contactId", listId)
            addToListIntent.putExtra("contactUsername", listName)
            startActivity(addToListIntent)
        }
    }

    inner class ListItem(val movieName: String, val movieImgURL: String, val listId: String): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.oneOfMyListsTextView.text = movieName
            Picasso.get().load(movieImgURL).into(viewHolder.itemView.oneOfMyListsImageView)

            viewHolder.itemView.oneOfMyListsImageView.setOnClickListener {
                layoutInf =
                    applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.chat_popup, null) as ViewGroup

                Picasso.get().load(movieImgURL).into(container.popUpChatImageView)
                container.movieChatNamePopUpTextView.text = movieName

                popUp = PopupWindow(container, 800, 1350, true)
                popUp.showAtLocation(myListLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                myListLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    myListLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

            }

            viewHolder.itemView.deleteListItemImageButton.setOnClickListener {
                db.collection("lists").document(listId).get().addOnSuccessListener {
                    val listItems = it["movies"] as Map<String, String>
                    if (listItems.size > 1) {
                        db.collection("lists").document(listId).update(
                            "movies." + movieName,
                            FieldValue.delete()
                        )
                        adapter.removeGroupAtAdapterPosition(position)
                    } else {
                        ///POTSER POSAR UN AVIS DE QUE ES BORRARA LA LLISTA
                        db.collection("lists").document(listId).delete()
                        val myListsIntent = Intent(viewHolder.itemView.deleteListItemImageButton.context,
                            MyListsActivity::class.java)
                        startActivity(myListsIntent)
                    }

                }

            }


        }

        override fun getLayout(): Int {
            return R.layout.one_of_my_lists_item
        }
    }

    private fun getListItems(listId: String){

        db.collection("lists").document(listId).get().addOnSuccessListener {

            val listItems = it["movies"] as MutableMap<String,String>

            adapter = GroupAdapter<GroupieViewHolder>()

            oneOfMyListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL,false)
            }

            for(key in listItems.keys){
                adapter.add(ListItem(key,listItems[key]!!, listId))
            }

            oneOfMyListsRecyclerView.adapter = adapter
        }
    }
}