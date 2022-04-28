package com.eugenisb.alphatest.lists

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eugenisb.alphatest.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_my_list.*
import kotlinx.android.synthetic.main.one_of_my_lists_item.view.*

class EditMyListActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_list)


        val listName = intent.extras?.getString("listName")
        val listId = intent.extras?.getString("listId")

        title = "Edit list:  $listName"

        if(listId != null)
            getList(listId)

        cancelEditListButton.setOnClickListener {
            onBackPressed()
        }

        acceptEditListButton.setOnClickListener {
            if(listId != null)
                editList(listId)
        }
    }

    private fun editList(listId: String) {

        db.collection("lists").document(listId).update(

            "name",  editListNameEditText.text.toString(),
            "public", editListCheckBox.isChecked
        )
        onBackPressed()
    }

    private fun getList(listId: String) {

        db.collection("lists").document(listId).get().addOnSuccessListener {

            editListCheckBox.isChecked = it["public"] as Boolean
            editListNameEditText.setText(it["name"].toString())
            val getMovieMap = it["movies"] as Map<String,String>
            Picasso.get().load(getMovieMap[getMovieMap.keys.elementAt(0)] as String).into(
                editListImageView)
            //////MAYBE PILLAR LA FOTO DE LA LLISTA O ALGO ASI

        }
    }


}