package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.R
import com.eugenisb.alphatest.SearchMovieAPIActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_lists.*
import kotlinx.android.synthetic.main.my_lists_item.view.*

class MyListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_lists)

        title = "My lists"
        val userId = FirebaseAuth.getInstance().uid

        getLists(userId!!)

    }

    inner class ListsItem(val listName: String, val listImgURL: String, val listNumberOfMovies: Int, val listId: String): Item<GroupieViewHolder>(){


        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.myListTextView.text = listName
            viewHolder.itemView.movieNumberTextView.text = "Movies/series: " + listNumberOfMovies.toString()
            Picasso.get().load(listImgURL).into(viewHolder.itemView.myListsImageView)


            viewHolder.itemView.myListsImageView.setOnClickListener {
                val myListItemIntent = Intent(viewHolder.itemView.myListsImageView.context,OneOfMyListsActivity::class.java)
                myListItemIntent.putExtra("listName", listName)
                myListItemIntent.putExtra("listId", listId)
                viewHolder.itemView.myListsImageView.context.startActivity(myListItemIntent)
            }

            viewHolder.itemView.deleteListImageButton.setOnClickListener {

                ////POSAR AVIS DE QUE ES BORRARA LA LLISTA
                db.collection("lists").document(listId).delete()
                adapter.removeGroupAtAdapterPosition(position)
                if(adapter.itemCount == 0){
                    noListsTextView.visibility = VISIBLE
                }

            }

            viewHolder.itemView.editListImageButton.setOnClickListener {
                val editListItemIntent = Intent(viewHolder.itemView.myListsImageView.context, EditMyListActivity::class.java)
                editListItemIntent.putExtra("listName", listName)
                editListItemIntent.putExtra("listId", listId)
                viewHolder.itemView.myListsImageView.context.startActivity(editListItemIntent)
            }

        }

        override fun getLayout(): Int {
            return R.layout.my_lists_item
        }
    }

    private fun getLists(userId: String) {

        db.collection("lists").whereEqualTo("creator", userId).get().addOnSuccessListener { results ->


            myListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            }

            if(results.documents.isNotEmpty()){
                for (document in results.documents) {
                    val getMovieMap = document["movies"] as Map<String, String>
                    val listName = document["name"] as String
                    val listImage = getMovieMap[getMovieMap.keys.elementAt(0)] as String
                    val listNumberOfMovies = getMovieMap.keys.size
                    adapter.add(ListsItem(listName, listImage, listNumberOfMovies, document.id))

                }

                /*adapter.setOnItemClickListener{ item,view ->
                    val contactItem = item as MyContactsActivity.ContactItem

                    val intentChatLog = Intent(view.context, ContactChatLogActivity::class.java)
                    intentChatLog.putExtra("contactId",item.userId)
                    intentChatLog.putExtra("contactUsername",item.username)
                    startActivity(intentChatLog)


                }
                */
                myListsRecyclerView.adapter = adapter
             }else{
                 noListsTextView.visibility = VISIBLE
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mylists_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.create_list ->{
                val createListIntent = Intent(this, CreateListActivity::class.java)
                startActivity(createListIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}