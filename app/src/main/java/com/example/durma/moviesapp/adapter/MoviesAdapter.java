package com.example.durma.moviesapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.durma.moviesapp.DetailActivity;
import com.example.durma.moviesapp.R;
import com.example.durma.moviesapp.model.Movie;

import java.util.List;

/**
 * Created by durma on 22.1.18..
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    private List<Movie> movieList;
    private Context context;

    public MoviesAdapter(List<Movie> movieList, Context context) {
        this.movieList = movieList;
        this.context = context;
    }



    @Override
    public MoviesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapter.ViewHolder holder, int position) {
        holder.title.setText(movieList.get(position).getOriginalTitle());
        String vote = Double.toString(movieList.get(position).getVoteAverage());
        holder.rating.setText(vote);

        Glide.with(context)
                .load(movieList.get(position).getPosterPath())
                .placeholder(R.drawable.loading)
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, rating;
        public ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
        title = (TextView)itemView.findViewById(R.id.titleCard);
        rating = (TextView)itemView.findViewById(R.id.userRatingCard);
        thumbnail = (ImageView)itemView.findViewById(R.id.thumbnailCard);

            itemView.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                 int pos = getAdapterPosition();
                 if (pos != RecyclerView.NO_POSITION){
                     Movie clickedDataItem = movieList.get(pos);
                     Intent intent = new Intent(context, DetailActivity.class);
                     intent.putExtra("movies", clickedDataItem );
                     intent.putExtra("id", movieList.get(pos).getId());
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     context.startActivity(intent);









                     /*Movie clickedMovie = movieList.get(pos);
                     Intent i = new Intent(context, DetailActivity.class);
                     i.putExtra("movies", clickedMovie );
                     i.putExtra("original_title", movieList.get(pos).getOriginalTitle());
                     i.putExtra("poster_path", movieList.get(pos).getPosterPath());
                     i.putExtra("overview", movieList.get(pos).getOverview());
                     i.putExtra("vote_average", movieList.get(pos).getVoteAverage());
                     i.putExtra("release_date", movieList.get(pos).getReleaseDate());
                     i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     context.startActivity(i);*/
                     Toast.makeText(view.getContext(), "Kliknuli smo " + clickedDataItem.getOriginalTitle(), Toast.LENGTH_LONG).show();


                     }

                }
            });
        }
    }
}
