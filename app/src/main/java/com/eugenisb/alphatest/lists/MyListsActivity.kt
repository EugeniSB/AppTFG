package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.contacts.ContactChatLogActivity
import com.eugenisb.alphatest.contacts.MyContactsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_contacts.*
import kotlinx.android.synthetic.main.activity_my_lists.*
import kotlinx.android.synthetic.main.movie_list_item.view.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import kotlinx.android.synthetic.main.my_lists_item.view.*

class MyListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_lists)

        title = "My lists"
        val userId = FirebaseAuth.getInstance().uid

        getLists(userId!!)

        createListfloatingActionButton.setOnClickListener {
            val createListIntent = Intent(this, CreateListActivity::class.java)
            startActivity(createListIntent)
        }
    }

    class ListsItem(val listName: String, val listImgURL: String, val listNumberOfMovies: Int): Item<GroupieViewHolder>(){

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myListTextView.text = listName
            viewHolder.itemView.movieNumberTextView.text = "Movies/series: " + listNumberOfMovies.toString()
            Picasso.get().load(listImgURL).into(viewHolder.itemView.myListsImageView)
        }

        override fun getLayout(): Int {
            return R.layout.my_lists_item
        }
    }

    private fun getLists(userId: String) {

        db.collection("lists").whereEqualTo("creator", userId).get().addOnSuccessListener {
                results ->
            val adapter = GroupAdapter<GroupieViewHolder>()

            myListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL,false)
            }

            for(document in results.documents){
                val getMovieMap = document["movies"] as Map<String,String>
                val listName = document["name"] as String
                val listImage = getMovieMap[getMovieMap.keys.elementAt(0)] as String
                val listNumberOfMovies = getMovieMap.keys.size
                adapter.add(ListsItem(listName,listImage,listNumberOfMovies))

                }

            adapter.setOnItemClickListener{ item,view ->
                val contactItem = item as MyContactsActivity.ContactItem

                val intentChatLog = Intent(view.context, ContactChatLogActivity::class.java)
                intentChatLog.putExtra("contactId",item.userId)
                intentChatLog.putExtra("contactUsername",item.username)
                startActivity(intentChatLog)
            }
            myListsRecyclerView.adapter = adapter
        }

    }
}