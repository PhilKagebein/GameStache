package com.example.videogamesearcher.api

import com.example.videogamesearcher.models.TwitchAccessToken
import retrofit2.Response
import retrofit2.http.*

interface TwitchApi {

    @POST("oauth2/token")
    suspend fun pushPostAccessToken(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String
    ): Response<TwitchAccessToken>
}