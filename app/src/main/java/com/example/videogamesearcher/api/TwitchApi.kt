package com.example.videogamesearcher.api

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.models.SearchResultsResponse
import com.example.videogamesearcher.models.TwitchAuthorization
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TwitchApi {

    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String
    ): Response<TwitchAuthorization>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("games")
    suspend fun searchGames(
        @Header("Authorization") accessToken: String,
        @Body gamesSearch: RequestBody
    ): Response<SearchResultsResponse>
}
