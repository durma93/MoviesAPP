package com.example.durma.moviesapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.durma.moviesapp.DetailActivity;
import com.example.durma.moviesapp.R;
import com.example.durma.moviesapp.model.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by durma on 30.1.18..
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w500";

    private List<Movie> movieList;
    private Context  context;

    public boolean isLoadingAdded = false;

    public PaginationAdapter(Context context) {
        this.movieList = new ArrayList<>();
        this.context = context;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater  = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingViewHolder(v2);
                break;
        }

        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater){

        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.movie_card, parent,false);
        viewHolder = new ViewHolderMovie(v1);
        return viewHolder;

    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie result = movieList.get(position);

        switch (getItemViewType(position)){
            case ITEM:
                final ViewHolderMovie movieVh= (ViewHolderMovie) holder;
                movieVh.title.setText(movieList.get(position).getOriginalTitle());
                String vote = Double.toString(movieList.get(position).getVoteAverage());
                movieVh.rating.setText(vote);

                String poster = "https://image.tmdb.org/t/p/w500" + movieList.get(position).getPosterPath();

                Glide.with(context)
                        .load(poster)
                        .listener(new RequestListener<String, GlideDrawable>() {

                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                movieVh.progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                //slika je spremna progres treba da nestane
                                movieVh.progressBar.setVisibility(View.GONE);
                                return false;// vraca se false ako zelim  da glide hendluje sve ostalo sam
                            }
                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//kesira sve
                        .placeholder(R.drawable.loading)
                        .into(movieVh.thumbnail);
                break;
            case LOADING:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movieList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void add (Movie r){
        movieList.add(r);
        notifyItemInserted(movieList.size()-1);
    }

    public void addAll(List<Movie> movies){
        for (Movie m : movies){
            add(m);
        }
    }

    private void remove(Movie r){

        int position = movieList.indexOf(r);
        if(position> -1){
            movieList.remove(position);
            notifyItemRemoved(position);
        }

    }

    public void clear(){
        isLoadingAdded = false;
        while (getItemCount() > 0){
            remove(getItem(0));
        }
    }
    public boolean isEmpty(){
        return getItemCount()==0;
    }

    public void removeLoadingFooter(){
        isLoadingAdded = false;

        int position = movieList.size() - 1;
        Movie movie = getItem(position);

        if (movie != null){
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Movie getItem(int position) {
        return movieList.get(position);
    }
    public class ViewHolderMovie extends RecyclerView.ViewHolder {
        public TextView title, rating;
        public ImageView thumbnail;
        ProgressBar progressBar;

        public ViewHolderMovie(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.titleCard);
            rating = (TextView)itemView.findViewById(R.id.userRatingCard);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnailCard);
            progressBar = (ProgressBar)itemView.findViewById(R.id.movie_progress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Movie clickedDataItem = movieList.get(pos);
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("movies", clickedDataItem );
                        intent.putExtra("id", movieList.get(pos).getId());
                        intent.putExtra("poster_path", movieList.get(pos).getPosterPath());
                        intent.putExtra("vote_average", movieList.get(pos).getVoteAverage());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        Toast.makeText(view.getContext(), "Kliknuli smo " + clickedDataItem.getOriginalTitle(), Toast.LENGTH_LONG).show();


                    }

                }
            });
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
