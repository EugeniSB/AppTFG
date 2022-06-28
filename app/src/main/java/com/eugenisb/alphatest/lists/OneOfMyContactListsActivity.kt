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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_one_of_my_contact_lists.*
import kotlinx.android.synthetic.main.activity_one_of_my_lists.*
import kotlinx.android.synthetic.main.chat_popup.view.*
import kotlinx.android.synthetic.main.one_of_my_contacts_lists_item.view.*
import kotlinx.android.synthetic.main.one_of_my_lists_item.view.*

class OneOfMyContactListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var popUp : PopupWindow
    private lateinit var layoutInf : LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_of_my_contact_lists)

        val listId = intent.extras?.getString("listId")
        val listName = intent.extras?.getString("listName")
        val contactName = intent.extras?.getString("contactName")

        //title = "List: $listName by $contactName"
        title = "$contactName's list: $listName"

        if(listId != null)
            getListItems(listId)

    }

    inner class ListItem(val movieName: String, val movieImgURL: String, val listId: String): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.oneOfMyContactsListsTextView.text = movieName
            Picasso.get().load(movieImgURL).into(viewHolder.itemView.oneOfMyContactsListsImageView)

            viewHolder.itemView.oneOfMyContactsListsImageView.setOnClickListener {
                layoutInf =
                    applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                var container = layoutInf.inflate(R.layout.chat_popup, null) as ViewGroup

                Picasso.get().load(movieImgURL).into(container.popUpChatImageView)
                container.movieChatNamePopUpTextView.text = movieName

                popUp = PopupWindow(container, 800, 1350, true)
                popUp.showAtLocation(contactListLayout, Gravity.CENTER, 0, 0)
                popUp.elevation = 100F
                contactListLayout.alpha = 0.5F
                //myOpinionsLayout.setBackgroundColor(Color.GRAY)

                popUp.setOnDismissListener {
                    contactListLayout.alpha = 1F
                    //myOpinionsLayout.setBackgroundColor(Color.WHITE)
                }

            }

        }

        override fun getLayout(): Int {
            return R.layout.one_of_my_contacts_lists_item
        }
    }


    private fun getListItems(listId: String){

        db.collection("lists").document(listId).get().addOnSuccessListener {

            val listItems = it["movies"] as MutableMap<String,String>

            val adapter = GroupAdapter<GroupieViewHolder>()

            oneOfMyContactsListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL,false)
            }

            for(key in listItems.keys){
                adapter.add(ListItem(key,listItems[key]!!, listId))
            }

            oneOfMyContactsListsRecyclerView.adapter = adapter
        }
    }


}