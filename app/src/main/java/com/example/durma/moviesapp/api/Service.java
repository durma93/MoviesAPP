package com.example.durma.moviesapp.api;


import com.example.durma.moviesapp.model.Movie;
import com.example.durma.moviesapp.model.MoviesResponse;
import com.example.durma.moviesapp.model.TrailerResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface Service {

    @GET("movie/popular")
    Call<MoviesResponse> getPupularMovies(@Query("api_key") String apikey);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovie(@Query("api_key") String apikey);

    @GET("movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("movie_id") int id, @Query("api_key") String apikey);


}
