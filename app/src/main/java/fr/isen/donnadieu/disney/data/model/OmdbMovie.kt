package fr.isen.donnadieu.disney.data.model

import com.google.gson.annotations.SerializedName

data class OmdbMovie(
    @SerializedName("Title") val title: String,
    @SerializedName("Year") val year: String,
    @SerializedName("Poster") val posterUrl: String,
    @SerializedName("Plot") val plot: String,
    @SerializedName("imdbRating") val rating: String
)