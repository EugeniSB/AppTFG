package com.eugenisb.alphatest.lists

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_contact_lists.*
import kotlinx.android.synthetic.main.contact_lists_item.view.*
import java.util.ArrayList

class AllContactsListsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_lists)

        title = "Contacts Lists"

        val userId = FirebaseAuth.getInstance().uid!!

        noAllListsTextView.visibility = VISIBLE

        getLists(userId)

    }

    inner class contactListsItem(val listName: String, val listImgURL: String,
                                 val listNumberOfMovies: Int, val listId: String, val listCreator: String): Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.contactListTextView.text = listName
            viewHolder.itemView.contactMovieNumberTextView.text =
                "Movies/series: " + listNumberOfMovies.toString()
            viewHolder.itemView.listCreatorTextView.visibility = VISIBLE
            viewHolder.itemView.listCreatorTextView.text = "By: $listCreator"
            Picasso.get().load(listImgURL).into(viewHolder.itemView.contactListsImageView)
        }

        override fun getLayout(): Int {
            return R.layout.contact_lists_item
        }
    }

    private fun getLists(userId: String) {

        db.collection("users").document(userId).get().addOnSuccessListener {

            val contactsIds = it["contacts"] as ArrayList<String>
            val contactsMap = mutableMapOf<String,String>()


            var thereIsLists = false

            contactListsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL,false)
            }
            if(contactsIds.isNotEmpty()) {
                for (id in contactsIds) {
                    db.collection("users").document(id).get().addOnSuccessListener {
                        contactsMap[id] = it["username"] as String

                        db.collection("lists").whereEqualTo("creator", id).get()
                            .addOnSuccessListener { results ->
                                for (document in results.documents) {
                                    val isPublic = document["public"] as Boolean
                                    if (isPublic) {
                                        noAllListsTextView.visibility = GONE
                                        val getMovieMap = document["movies"] as Map<String, String>
                                        val listName = document["name"] as String
                                        val listImage =
                                            getMovieMap[getMovieMap.keys.elementAt(0)] as String
                                        val listNumberOfMovies = getMovieMap.keys.size
                                        adapter.add(
                                            contactListsItem(
                                                listName,
                                                listImage,
                                                listNumberOfMovies,
                                                document.id,
                                                contactsMap[id]!!
                                            )
                                        )
                                    }
                                }

                                adapter.setOnItemClickListener { item, view ->
                                    val contactListItem =
                                        item as AllContactsListsActivity.contactListsItem

                                    val intentContactList = Intent(
                                        view.context,
                                        OneOfMyContactListsActivity::class.java
                                    )
                                    intentContactList.putExtra("listId", item.listId)
                                    intentContactList.putExtra("listName", item.listName)
                                    intentContactList.putExtra("contactName", item.listCreator)
                                    startActivity(intentContactList)

                                }

                                contactListsRecyclerView.adapter = adapter
                            }
                    }

                }

            }else{
                noAllListsContactsTextView.visibility = VISIBLE
                noAllListsTextView.visibility = GONE
            }

            }
        }
 }


