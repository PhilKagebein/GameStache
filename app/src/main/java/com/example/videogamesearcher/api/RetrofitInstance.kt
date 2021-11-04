package com.example.videogamesearcher.api

import com.example.videogamesearcher.Constants.Companion.AUTH_URL
import com.example.videogamesearcher.Constants.Companion.BASE_URL
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

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: TwitchApi by lazy {
        retrofit.create(TwitchApi::class.java)
    }
}