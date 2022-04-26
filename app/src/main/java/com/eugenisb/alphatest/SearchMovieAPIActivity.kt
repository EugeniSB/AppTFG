package com.eugenisb.alphatest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.eugenisb.alphatest.adapters.MovieResultsAdapter
import com.eugenisb.alphatest.clases.Results
import com.eugenisb.alphatest.listeners.OnMovieClickListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_searchmovieapi.*
import kotlinx.android.synthetic.main.my_contacts_item.view.*
import okhttp3.*
import java.io.IOException

class SearchMovieAPIActivity : AppCompatActivity(), OnMovieClickListener {

    private lateinit var adapter: MovieResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchmovieapi)


        val screen = intent.extras?.getString("screen")!!

        title = "Search movie/series"
        if(screen == "createList"){
            title = "Add first movie/series to list"
        }


        API_search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {

                callAPI(query!!, screen)


                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })



/*
        val client = OkHttpClient()
        buttonAPI.setOnClickListener {
            var movieName = ""
            movieName = APITEXT.text.toString()
            val request = Request.Builder()
                .url("https://api.themoviedb.org/3/search/movie?api_key=60494e2f64dd85ff91f57a111d1d7f2e&query=$movieName&page=1")//"https://mdblist.p.rapidapi.com/?s=jaws")
                .build()

            client.newCall(request).enqueue(object : Callback{
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()

                    val gson = GsonBuilder().create()

                    val  movies = gson.fromJson(body, Results::class.java)

                    println(movies.results[0].original_title)
                    println(movies.results[0].poster_path)

                    runOnUiThread {
                        APItextview.text = movies.results[0].original_title
                        Picasso.get().load("https://image.tmdb.org/t/p/original/" + movies.results[0].poster_path).into(APIimageView)
                    }

                }

                override fun onFailure(call: Call, e: IOException) {
                    println("FAILED TO REQUEST " + e.toString())
                }

            })

        }





    }

    class Results(val results: List<Movie>){

    }

    class Movie(val original_title: String, val poster_path: String)

     */

    }

    private fun callAPI (movieName : String, screen: String){


            val client = OkHttpClient()

            val contactId = intent.extras?.getString("contactId") ?: ""
            val contactUsername = intent.extras?.getString("contactUsername") ?: ""

            val request = Request.Builder()
                .url("https://api.themoviedb.org/3/search/multi?api_key=60494e2f64dd85ff91f57a111d1d7f2e&query=$movieName")//"https://mdblist.p.rapidapi.com/?s=jaws")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()

                    val gson = GsonBuilder().create()

                    val movies = gson.fromJson(body, Results::class.java)

                    runOnUiThread {
                        var int = 0
                        for (movie in movies.results.indices){
                            if (movies.results[int].vote_count < 200){
                                movies.results.removeAt(int)
                            }
                            else
                                int += 1
                        }
                        showResult(movies, contactId, contactUsername, screen)
                    }

                }

                override fun onFailure(call: Call, e: IOException) {
                    println("FAILED TO REQUEST " + e.toString())
                }

            })

    }

    private fun showResult(results: Results, contactId: String , contactUsername: String, screen: String){
        API_recycler_view.setHasFixedSize(true)
        API_recycler_view.layoutManager = GridLayoutManager(this, 2)
        adapter = MovieResultsAdapter(this, results.results, this, contactId, contactUsername, screen)
        API_recycler_view.adapter = adapter
    }

    override fun onMovieClicked(name: String) {
        Toast.makeText(this, name, Toast.LENGTH_SHORT)
    }
}