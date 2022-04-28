package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import kotlinx.android.synthetic.main.one_of_my_contacts_lists_item.view.*
import kotlinx.android.synthetic.main.one_of_my_lists_item.view.*

class OneOfMyContactListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_of_my_contact_lists)

        val listId = intent.extras?.getString("listId")
        val listName = intent.extras?.getString("listName")

        title = "List: $listName"

        if(listId != null)
            getListItems(listId)

    }

    inner class ListItem(val movieName: String, val movieImgURL: String, val listId: String): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.oneOfMyContactsListsTextView.text = movieName
            Picasso.get().load(movieImgURL).into(viewHolder.itemView.oneOfMyContactsListsImageView)

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