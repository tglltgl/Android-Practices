package com.example.praktica3.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


data class VideoResponse(
    val id: Int,
    val title: String,
    val description: String,
    val image: String,
    val category: String
)

interface FootballApi {
    @GET("products/{id}")
    suspend fun getVideo(@Path("id") id: Int): VideoResponse
}

object Network {
    private const val BASE_URL = "https://fakestoreapi.com/"

    val api: FootballApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApi::class.java)
    }
}