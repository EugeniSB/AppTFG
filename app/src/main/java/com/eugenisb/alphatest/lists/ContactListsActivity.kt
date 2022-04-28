package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_contact_lists.*
import kotlinx.android.synthetic.main.activity_my_lists.*
import kotlinx.android.synthetic.main.contact_lists_item.view.*
import kotlinx.android.synthetic.main.my_lists_item.view.*

class ContactListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_lists)

        val contactId = intent.extras?.getString("contactId")
        val contactUsername = intent.extras?.getString("contactUsername")

        title = contactUsername + " lists"

        if(contactId != null)
            getContactLists(contactId)
    }


    inner class contactListsItem(val listName: String, val listImgURL: String, val listNumberOfMovies: Int, val listId: String): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactListTextView.text = listName
            viewHolder.itemView.contactMovieNumberTextView.text =
                "Movies/series: " + listNumberOfMovies.toString()
            Picasso.get().load(listImgURL).into(viewHolder.itemView.contactListsImageView)
        }

        override fun getLayout(): Int {
            return R.layout.contact_lists_item
        }
    }


    private fun getContactLists(contactId: String) {

        db.collection("lists").whereEqualTo("creator", contactId).get().addOnSuccessListener {
                results ->
            val adapter = GroupAdapter<GroupieViewHolder>()

            contactListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL,false)
            }

            for(document in results.documents){
                val isPublic = document["public"] as Boolean
                if(isPublic){
                    val getMovieMap = document["movies"] as Map<String,String>
                    val listName = document["name"] as String
                    val listImage = getMovieMap[getMovieMap.keys.elementAt(0)] as String
                    val listNumberOfMovies = getMovieMap.keys.size
                    adapter.add(contactListsItem(listName,listImage,listNumberOfMovies, document.id))
                }

            }

            adapter.setOnItemClickListener{ item,view ->
                val contactListItem = item as contactListsItem

                val intentContactList = Intent(view.context, OneOfMyContactListsActivity::class.java)
                intentContactList.putExtra("listId",item.listId)
                intentContactList.putExtra("listName",item.listName)
                startActivity(intentContactList)

            }

            contactListsRecyclerView.adapter = adapter
        }

    }
}


