package com.example.durma.moviesapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.durma.moviesapp.Data.FavoriteDbHelper;
import com.example.durma.moviesapp.adapter.MoviesAdapter;
import com.example.durma.moviesapp.adapter.PaginationAdapter;
import com.example.durma.moviesapp.api.Client;
import com.example.durma.moviesapp.api.Service;
import com.example.durma.moviesapp.model.Movie;
import com.example.durma.moviesapp.model.MoviesResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    private SwipeRefreshLayout swipeContainer;
    public static final String TAG = "MOVIE APP";

    private FavoriteDbHelper favoriteDbHelper;

    //dodavanje cache-a
    int cacheSize = 10*1024*1024;

    //dodavanje paginacije

    PaginationAdapter paginationAdapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private int TOTAL_PAGES = 5;
    private int current_page = PAGE_START;

    private Service movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();



    }
    @SuppressLint("ResourceAsColor")
    private void initViews(){

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);

        paginationAdapter = new PaginationAdapter(this);

        linearLayoutManager = new GridLayoutManager(this, 2);

        rv.setLayoutManager(linearLayoutManager);

        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(paginationAdapter);

        rv.addOnScrollListener(new PaginationScrollListener((GridLayoutManager) linearLayoutManager) {
            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public void loadMoreItems() {
                isLoading = true;
                current_page +=1;

                //mokovanje kasnjenje mreze za API poziv od jedne sekunde
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMoreItems();
                    }
                }, 1000);
            }
        });

        movieService = Client.getClient().create(Service.class);


        /*
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(movieList, this);
        adapter.notifyDataSetChanged();

        favoriteDbHelper = new FavoriteDbHelper(MainActivity.this);


        //upotrebljen getApplicationContext() umesto getActivity() metode
        if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_content);
        swipeContainer.setColorSchemeColors(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MainActivity.this,"Movies Refreshed",Toast.LENGTH_LONG).show();
            }
        });
*/

        checkSortOrder();
    }

    private Call<MoviesResponse> callTopRatedMoviesApi(){

        return movieService.getTopRatedMovie(BuildConfig.THE_MOVIE_DB_API_TOKEN,
                current_page);

    }

    private Call<MoviesResponse> callPupularMoviesMoviesApi(){

        return movieService.getPupularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN,
                current_page);

    }


    private boolean checkConectionAvalable(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

    private void loadJSON() {

        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(),"Nije ubacen API kljuc", Toast.LENGTH_LONG).show();
                return;
            }


            //Cache dodat
            Cache cache = new Cache(getCacheDir(), cacheSize);

            OkHttpClient okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .cache(cache)
                    .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain)
                        throws IOException {

                    Request request = chain.request();
                    if(!checkConectionAvalable()){
                        int maxStale = 60*60*24*28; //tolerate 4 nedelje stale
                        request = request
                                .newBuilder()
                                .header("Cache-Control", "public, only-if-cached, max-stale" + maxStale)
                                .build();
                    }


                    return chain.proceed(request);
                }
            }).build();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://api.themoviedb.org/3/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            Service apiService= retrofit.create(Service.class);
/*

            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
*/

            Call<MoviesResponse> call = apiService.getPupularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getMovies();

                    //Colection za sort
                    Collections.sort(movies, Movie.BY_NAME_ALPHABETICAL);

                    recyclerView.setAdapter(new MoviesAdapter(movies, getApplicationContext()));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()){
                        swipeContainer.setRefreshing(false);
                    }

                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", "onFailure je pozvan iz enquea" );
                    Toast.makeText(MainActivity.this, "onFailure je pozvan iz enquea", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Log.d("Error", "Greska u try bloku");
            Toast.makeText(this, "Greska u try bloku", Toast.LENGTH_LONG).show();
        }

    }

    private void loadJSON1() {

        try{
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()){
                Toast.makeText(getApplicationContext(),"Nije ubacen API kljuc", Toast.LENGTH_LONG).show();
                return;
            }

            //Cache dodat
            Cache cache = new Cache(getCacheDir(), cacheSize);

            OkHttpClient okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .cache(cache)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain)
                                throws IOException {

                            Request request = chain.request();
                            if(!checkConectionAvalable()){
                                int maxStale = 60*60*24*28; //tolerate 4 nedelje stale
                                request = request
                                        .newBuilder()
                                        .header("Cache-Control", "public, only-if-cached, max-stale" + maxStale)
                                        .build();
                            }


                            return chain.proceed(request);
                        }
                    }).build();

            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl("http://api.themoviedb.org/3/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());

            Retrofit retrofit = builder.build();
            Service apiService= retrofit.create(Service.class);

/*
            Client client = new Client();
            Service apiService = Client.getClient().create(Service.class);
*/

            Call<MoviesResponse> call = apiService.getTopRatedMovie(BuildConfig.THE_MOVIE_DB_API_TOKEN);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getMovies();

                    //Colection za sort
                    Collections.sort(movies, Movie.BY_NAME_ALPHABETICAL);

                    recyclerView.setAdapter(new MoviesAdapter(movies, getApplicationContext()));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()){
                        swipeContainer.setRefreshing(false);
                    }

                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", "onFailure je pozvan iz enquea" );
                    Toast.makeText(MainActivity.this, "onFailure je pozvan iz enquea", Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Log.d("Error", "Greska u try bloku");
            Toast.makeText(this, "Greska u try bloku", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //videcemo da l' radi bez ovoga ----RADIIII
    public Activity getActivity(){
        Context context = this;
        while (context instanceof ContextWrapper){
            if (context instanceof Activity){
                return (Activity) context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(TAG, "Preferences updated");
        checkSortOrder();
    }

    private void checkSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOreder= preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );

        if (sortOreder.equals(this.getString(R.string.pref_most_popular))) {
            Log.d(TAG, "Sorting najpopularniji");
            loadJSON();

        }else if(sortOreder.equals(this.getString(R.string.favorite))){

            Log.d(TAG, "Sorting by favorite");
            initViews2();

        }else {
            Log.d(TAG, "Sorting vote average");

            loadJSON1();
        }
    }

    //za favoirte
    private void initViews2() {

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();

        adapter = new MoviesAdapter(movieList, this);

        if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        favoriteDbHelper = new FavoriteDbHelper(MainActivity.this);

        getAllfavorite();
    }

    @SuppressLint("StaticFieldLeak")
    private void getAllfavorite() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                movieList.clear();
                movieList.addAll(favoriteDbHelper.getAllFavorite());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                super.onPostExecute(aVoid);

                adapter.notifyDataSetChanged();
            }
        }.execute();



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (movieList.isEmpty()){
            checkSortOrder();
        }else {

        }
    }
}
