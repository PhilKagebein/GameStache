package com.example.videogamesearcher.api

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.models.*
import com.example.videogamesearcher.models.explore_spinners.SpinnerResponse
import com.example.videogamesearcher.models.search_results.SearchResultsResponse
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

    @Headers("Client-ID: $CLIENT_ID")
    @POST("platforms")
    suspend fun getPlatformsList(
        @Header("Authorization") accessToken: String,
        @Body platformsBody: RequestBody
    ): Response<SpinnerResponse>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("genres")
    suspend fun getGenresList(
        @Header("Authorization") accessToken: String,
        @Body platformsBody: RequestBody
    ): Response<SpinnerResponse>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("game_modes")
    suspend fun getGameModesList(
        @Header("Authorization") accessToken: String,
        @Body platformsBody: RequestBody
    ): Response<SpinnerResponse>
}
