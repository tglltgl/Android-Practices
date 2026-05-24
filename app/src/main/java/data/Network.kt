package com.example.praktica3.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


data class SportsVideoResponse(
    val tvshows: List<VideoItem>?
)

data class VideoItem(
    val idEvent: String?,
    val strEvent: String?,
    val strSport: String?,
    val strVideo: String?,
    val strThumb: String?
)

interface FootballApi {

    @GET("eventsallvideo.php")
    suspend fun getLatestVideos(): SportsVideoResponse
}

object Network {
    private const val BASE_URL = "https://www.thesportsdb.com/api/v1/json/3/"

    val api: FootballApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApi::class.java)
    }
}