package fr.isen.donnadieu.disney.data.api

import fr.isen.donnadieu.disney.data.model.OmdbMovie
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {
    @GET("/")
    suspend fun getMovieById(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String = "f3553feb"
    ): OmdbMovie
}