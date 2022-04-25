package com.eugenisb.alphatest.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.eugenisb.alphatest.R;
import com.eugenisb.alphatest.auth.AuthActivity;
import com.eugenisb.alphatest.clases.Movies;
import com.eugenisb.alphatest.clases.Results;
import com.eugenisb.alphatest.contacts.ContactRecommendationActivity;
import com.eugenisb.alphatest.groups.GroupRecommendationActivity;
import com.eugenisb.alphatest.listeners.OnMovieClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieResultsAdapter extends RecyclerView.Adapter<HomeViewHolder> {

    Context context;
    List<Movies> result_list;
    OnMovieClickListener listener;
    String contactId;
    String contactUsername;
    String screen;

    public MovieResultsAdapter(Context context, List<Movies> result_list, OnMovieClickListener listener,
                               String contactId, String contactUsername, String screen) {
        this.context = context;
        this.result_list = result_list;
        this.listener = listener;
        this.contactId = contactId;
        this.contactUsername = contactUsername;
        this.screen = screen;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeViewHolder(LayoutInflater.from(context).inflate(R.layout.movie_list_item,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {

        if (result_list.get(position).getVote_count() > 200) {
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
                        recommendIntent.putExtra("movieName", moviename);
                        recommendIntent.putExtra("contactId", contactId);
                        recommendIntent.putExtra("contactUsername", contactUsername);
                        context.startActivity(recommendIntent);

                    }else if(screen.equals("group")){
                        Intent recommendIntent = new Intent(context, GroupRecommendationActivity.class);
                        recommendIntent.putExtra("movieName", moviename);
                        recommendIntent.putExtra("contactId", contactId);
                        recommendIntent.putExtra("contactUsername", contactUsername);
                        context.startActivity(recommendIntent);

                    }else if(screen.equals("lists")){

                    }else {

                    }

                }
            });
        }

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