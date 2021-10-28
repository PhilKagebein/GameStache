package com.example.videogamesearcher.api

import com.example.videogamesearcher.Constants.Companion.AUTH_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofitAccessToken by lazy {
        Retrofit.Builder()
            .baseUrl(AUTH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiAccessToken: TwitchApi by lazy {
        retrofitAccessToken.create(TwitchApi::class.java)
    }
}