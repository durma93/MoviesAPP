package com.example.durma.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.durma.moviesapp.adapter.TrailerAdapter;
import com.example.durma.moviesapp.api.Client;
import com.example.durma.moviesapp.api.Service;
import com.example.durma.moviesapp.model.Movie;
import com.example.durma.moviesapp.model.Trailer;
import com.example.durma.moviesapp.model.TrailerResponse;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by durma on 22.1.18..
 */

public class DetailActivity extends AppCompatActivity {
    TextView nameOfMovie, plotSynopsis, userRating, releaseDate;
    ImageView imageView;

    private RecyclerView recyclerView;
    private TrailerAdapter trailerAdapter;
    private List<Trailer> trailerList;


    Movie movie;
    String thumbnail, movieName, synopsis, rating, dateOfRelease;
    int movie_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initColapsingToolbar();

        initViews();


        imageView = (ImageView)findViewById(R.id.thumbnail_image_header);
        nameOfMovie = (TextView) findViewById(R.id.title);
        plotSynopsis = (TextView) findViewById(R.id.plot_synopsis);
        userRating = (TextView) findViewById(R.id.user_rating);
        releaseDate = (TextView) findViewById(R.id.realise_date);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

            movie = (Movie) getIntent().getSerializableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();
           /* thumbnail = getIntent().getExtras().getString("poster_path");
            movieName = getIntent().getExtras().getString("original_title");
            synopsis = getIntent().getExtras().getString("overview");
            rating = getIntent().getExtras().getString("vote_average");
            dateOfRelease = getIntent().getExtras().getString("release_date");*/

            Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.loading)
                    .into(imageView);

            nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);

        }else{
            Toast.makeText(this, "Nema api podatka iz moviesAdaptera", Toast.LENGTH_LONG).show();
        }
    }

    private void initColapsingToolbar() {

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbarLayout.setTitle(" ");

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset ==0){
                    collapsingToolbarLayout.setTitle(getString(R.string.movie_details));
                    isShow = true;

                }else if(isShow){
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });

        //za favoite movies

        MaterialFavoriteButton materialFavoriteButton = (MaterialFavoriteButton) findViewById(R.id.favourite_button);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                if (favorite){
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.durma.moviesapp.DetailActivity", MODE_PRIVATE).edit();
                    editor.putBoolean("Favourite added",true);
                    editor.commit();
                    //saveFavorite();

                    Snackbar.make(buttonView, "Added to favorite", Snackbar.LENGTH_LONG).show();
                }else {
                    SharedPreferences.Editor editor = getSharedPreferences("com.example.durma.moviesapp.DetailActivity", MODE_PRIVATE).edit();
                    editor.putBoolean("Favorite Removed",true);
                    editor.commit();

                    Snackbar.make(buttonView, "Removed from favorite", Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

/*
    pocetak implementacije za trailer
*/
    private void initViews(){

        trailerList = new ArrayList<>();
        trailerAdapter = new TrailerAdapter(this, trailerList);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(trailerAdapter);
        trailerAdapter.notifyDataSetChanged();

        loadJSON();


    }

    private void loadJSON() {
        int movie_id = getIntent().getExtras().getInt("id");

        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(), "Nema Api kljuca", Toast.LENGTH_LONG).show();

            }
            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);

                Call<TrailerResponse> call = apiService.getMovieTrailer(movie_id, BuildConfig.THE_MOVIE_DB_API_TOKEN);
                call.enqueue(new Callback<TrailerResponse>() {
                    @Override
                    public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                        List<Trailer> trailers = response.body().getResults();
                        recyclerView.setAdapter(new TrailerAdapter(getApplicationContext(), trailers));
                        recyclerView.smoothScrollToPosition(0);
                    }

                    @Override
                    public void onFailure(Call<TrailerResponse> call, Throwable t) {
                        Log.d("Error", "onFailure: JSON TRAILER");
                        Toast.makeText(DetailActivity.this, "Error onFailure: JSON TRAILER ", Toast.LENGTH_LONG).show();
                    }
                });

        }catch (Exception e){
                Log.d("Error", "Catch u JSonu ");
                Toast.makeText(this, "Catch u JSonu ", Toast.LENGTH_LONG).show();
        }

    }


}
