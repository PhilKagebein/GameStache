package com.example.gamestache.api

import com.example.gamestache.Constants.Companion.CLIENT_ID
import com.example.gamestache.models.*
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem
import com.example.gamestache.models.individual_game.IndividualGameDataItem
import com.example.gamestache.models.search_results.SearchResultsResponse
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
        @Body platformsPostRequestBody: RequestBody
    ): Response<List<PlatformsResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("genres")
    suspend fun getGenresList(
        @Header("Authorization") accessToken: String,
        @Body genresPostRequestBody: RequestBody
    ): Response<List<GenresResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("game_modes")
    suspend fun getGameModesList(
        @Header("Authorization") accessToken: String,
        @Body gameModesPostRequestBody: RequestBody
    ): Response<List<GameModesResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("games")
    suspend fun getIndividualGameData(
        @Header("Authorization") accessToken: String,
        @Body individualGamePostRequestBody: RequestBody
    ): Response<List<IndividualGameDataItem>>
}
