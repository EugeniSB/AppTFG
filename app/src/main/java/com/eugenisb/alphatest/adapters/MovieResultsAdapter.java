package com.eugenisb.alphatest.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.BoolRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.eugenisb.alphatest.R;
import com.eugenisb.alphatest.clases.Movies;
import com.eugenisb.alphatest.contacts.ContactRecommendationActivity;
import com.eugenisb.alphatest.groups.GroupRecommendationActivity;
import com.eugenisb.alphatest.listeners.OnMovieClickListener;
import com.eugenisb.alphatest.lists.MyListsActivity;
import com.eugenisb.alphatest.lists.OneOfMyListsActivity;
import com.eugenisb.alphatest.opinions.CreateOpinionActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieResultsAdapter extends RecyclerView.Adapter<HomeViewHolder> {

    Context context;
    List<Movies> result_list;
    OnMovieClickListener listener;
    String contactId;
    String contactUsername;
    String screen;
    Boolean isPublic;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MovieResultsAdapter(Context context, List<Movies> result_list, OnMovieClickListener listener,
                               String contactId, String contactUsername, String screen,
                               Boolean isPublic) {
        this.context = context;
        this.result_list = result_list;
        this.listener = listener;
        this.contactId = contactId;
        this.contactUsername = contactUsername;
        this.screen = screen;
        this.isPublic = isPublic;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeViewHolder(LayoutInflater.from(context).inflate(R.layout.movie_list_item,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {

            if (result_list.get(position).getOriginal_title() != "") {
                holder.movie_item_title.setText(result_list.get(position).getOriginal_title());
            } else {
                holder.movie_item_title.setText(result_list.get(position).getOriginal_name());
            }

            if (result_list.get(position).getPoster_path() != null) {
                Picasso.get().load("https://image.tmdb.org/t/p/original/" + result_list.get(position).getPoster_path()).into(holder.movie_item_image_view);
            } else {
                Picasso.get().load(R.drawable.circle).into(holder.movie_item_image_view);
            }

            holder.movie_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String moviename = holder.movie_item_title.getText().toString();
                    System.out.println(moviename);

                    if(screen.equals("user")){
                        Intent recommendIntent = new Intent(context, ContactRecommendationActivity.class);
                        recommendIntent.putExtra("movieName", moviename.replace("."," "));
                        recommendIntent.putExtra("contactId", contactId);
                        recommendIntent.putExtra("contactUsername", contactUsername);
                        context.startActivity(recommendIntent);

                    }else if(screen.equals("group")){
                        Intent recommendIntent = new Intent(context, GroupRecommendationActivity.class);
                        recommendIntent.putExtra("movieName", moviename.replace("."," "));
                        recommendIntent.putExtra("groupId", contactId);
                        recommendIntent.putExtra("contactUsername", contactUsername);
                        context.startActivity(recommendIntent);

                    }else if(screen.equals("list")){

                        addToList(contactId, moviename, "https://image.tmdb.org/t/p/original/" + result_list.get(position).getPoster_path());
                        Intent myListIntent = new Intent(context, OneOfMyListsActivity.class);
                        myListIntent.putExtra("listName", contactUsername);
                        myListIntent.putExtra("listId", contactId);
                        context.startActivity(myListIntent);

                    }else if (screen.equals("createList")){
                        createList(contactUsername,
                                "https://image.tmdb.org/t/p/original/" + result_list.get(position).getPoster_path(),
                                moviename, isPublic);
                        Intent myListsIntent = new Intent(context, MyListsActivity.class);
                        context.startActivity(myListsIntent);
                    }else if (screen.equals("opinion")){
                        Intent createOpinionIntent = new Intent(context, CreateOpinionActivity.class);
                        createOpinionIntent.putExtra("movieName", moviename);
                        createOpinionIntent.putExtra("moviePoster", "https://image.tmdb.org/t/p/original/" +
                                result_list.get(position).getPoster_path());
                        context.startActivity(createOpinionIntent);
                    }

                }
            });

    }

    private void addToList(String listId, String movieName, String movieImg) {

        db.collection("lists")
                .document(listId).get().addOnSuccessListener(
                documentSnapshot -> {
                    Map<String,String> getMovieList = (Map<String, String>) documentSnapshot.get("movies");

                    String newMovieName = movieName.replace("."," ");


                    if(!getMovieList.keySet().contains(newMovieName)) {
                        db.collection("lists").document(listId).update(
                                "movies." + newMovieName, movieImg);
                    }else{
                        //////PRINTEEAR ERROR DE QUE LA PELI JA ESTA A LA LLISTA O ALGO AIXI
                    }
                }
        );

    }

    private void createList(String listName, String movieImg, String movieName, Boolean isPublic ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String,String> lists = new HashMap<>();
        lists.put(movieName.replace("."," "),movieImg);

        Map<String, Object> list = new HashMap<>();
        list.put("name", listName);
        list.put("creator", user.getUid());
        list.put("movies", lists);
        list.put("public", isPublic);

        db.collection("lists").add(list);
    }

    @Override
    public int getItemCount() {

        return result_list.size();
    }
}

class HomeViewHolder extends RecyclerView.ViewHolder{

    ImageView movie_item_image_view;
    TextView movie_item_title;
    CardView movie_item;

    public HomeViewHolder(@NonNull View itemView) {
        super(itemView);
        movie_item = itemView.findViewById(R.id.movie_item);
        movie_item_image_view = itemView.findViewById(R.id.movie_item_image_view);
        movie_item_title = itemView.findViewById(R.id.movie_item_title);
    }
}