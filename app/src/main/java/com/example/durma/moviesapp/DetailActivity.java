package com.example.durma.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.durma.moviesapp.Data.FavoriteContract;
import com.example.durma.moviesapp.Data.FavoriteDbHelper;
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

    private FavoriteDbHelper favoriteDbHelper;
    private SQLiteDatabase mdb;
    private Movie movie_favorite;
    private final AppCompatActivity activity = DetailActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Sredjivanje dugmeta za omiljene filmove da ostane u zadatom stanju
        favoriteDbHelper = new FavoriteDbHelper(this);
        mdb = favoriteDbHelper.getWritableDatabase();




        imageView = (ImageView)findViewById(R.id.thumbnail_image_header);
        //nameOfMovie = (TextView) findViewById(R.id.title);
        plotSynopsis = (TextView) findViewById(R.id.plotsynopsis);
        userRating = (TextView) findViewById(R.id.userrating);
        releaseDate = (TextView) findViewById(R.id.releasedate);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")){

            movie = (Movie) getIntent().getSerializableExtra("movies");

            thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            synopsis = movie.getOverview();
            rating = Double.toString(movie.getVoteAverage());
            dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();

           String poster = "https://image.tmdb.org/t/p/w500" + thumbnail;

            Glide.with(this)
                    .load(poster)
                    .placeholder(R.drawable.loading)
                    .into(imageView);

           // nameOfMovie.setText(movieName);
            plotSynopsis.setText(synopsis);
            userRating.setText(rating);
            releaseDate.setText(dateOfRelease);

            ( (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(movieName);

/*            //Dodavanje da se gubi ime filma kad se skroluje
            AppBarLayout appBarLayout =(AppBarLayout) findViewById(R.id.appbar);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {


                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (Math.abs(verticalOffset)==appBarLayout.getTotalScrollRange() ){
                        //Promene u dinamickom skrolovanju Detail aktivitija
                        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(movieName);
                    } else {
                        ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle("");
                    }
                }
            });*/
        }else{
            Toast.makeText(this, "Nema api podatka iz moviesAdaptera", Toast.LENGTH_LONG).show();
        }

        //hendlovanje

        MaterialFavoriteButton materialFavoriteButton = (MaterialFavoriteButton) findViewById(R.id.favorite_button);

        if (Exist(movieName)){
            materialFavoriteButton.setFavorite(true);
            materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                    if(favorite == true){
                        saveFavorite();
                        Snackbar.make(buttonView, "Added to favorite", Snackbar.LENGTH_LONG).show();
                    }else {
                        favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                        favoriteDbHelper.deleteFavorite(movie_id);
                        Snackbar.make(buttonView, "Removed from favorite", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            materialFavoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                    if(favorite == true){
                        saveFavorite();
                        Snackbar.make(buttonView, "Added to favorite", Snackbar.LENGTH_LONG).show();
                    }else {
                        int movie_id = getIntent().getExtras().getInt("id");
                        favoriteDbHelper = new FavoriteDbHelper(DetailActivity.this);
                        favoriteDbHelper.deleteFavorite(movie_id);
                        Snackbar.make(buttonView, "Removed from favorite", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }

        initViews();

    }

    public boolean Exist(String searchItem) {

        String[] projection = {
                FavoriteContract.FavoriteEntry._ID,
                FavoriteContract.FavoriteEntry.COLUMN_MOVIEID,
                FavoriteContract.FavoriteEntry.COLUMN_TITLE,
                FavoriteContract.FavoriteEntry.COLUMN_USERRATING,
                FavoriteContract.FavoriteEntry.COLUMN_POSTER_PATH,
                FavoriteContract.FavoriteEntry.COLUMN_PLOT_SYNOPSIS

        };
        String selection = FavoriteContract.FavoriteEntry.COLUMN_TITLE + " =?";
        String[] selectionArgs = { searchItem };
        String limit = "1";

        Cursor cursor = mdb.query(FavoriteContract.FavoriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null, limit);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
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
        //int movie_id = getIntent().getExtras().getInt("id");

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


    private void saveFavorite() {
        favoriteDbHelper = new FavoriteDbHelper(activity);


        movie_favorite = new Movie();

        Double rate = movie.getVoteAverage();

        movie_favorite.setId(movie_id);
        movie_favorite.setOriginalTitle(movieName);
        movie_favorite.setPosterPath(thumbnail);
        movie_favorite.setVoteAverage(rate);
        movie_favorite.setOverview(synopsis);

        favoriteDbHelper.addFavorite(movie_favorite);

    }

}
