package com.stache.gamestache.api

import com.stache.gamestache.Constants.Companion.CLIENT_ID
import com.stache.gamestache.models.explore_spinners.GameModesResponseItem
import com.stache.gamestache.models.explore_spinners.GenresResponseItem
import com.stache.gamestache.models.explore_spinners.PlatformsResponseItem
import com.stache.gamestache.models.individual_game.IndividualGameDataItem
import com.stache.gamestache.models.search_results.SearchResultsResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface TwitchApi {

    @Headers("Client-ID: $CLIENT_ID")
    @POST("games")
    suspend fun searchForGames(
        @Header("Authorization") authToken: String,
        @Body gamesSearch: RequestBody
    ): Response<SearchResultsResponse>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("platforms")
    suspend fun getPlatformsList(
        @Header("Authorization") authToken: String,
        @Body platformsPostRequestBody: RequestBody
    ): Response<List<PlatformsResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("genres")
    suspend fun getGenresList(
        @Header("Authorization") authToken: String,
        @Body genresPostRequestBody: RequestBody
    ): Response<List<GenresResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("game_modes")
    suspend fun getGameModesList(
        @Header("Authorization") authToken: String,
        @Body gameModesPostRequestBody: RequestBody
    ): Response<List<GameModesResponseItem>>

    @Headers("Client-ID: $CLIENT_ID")
    @POST("games")
    suspend fun getIndividualGameData(
        @Header("Authorization") authToken: String,
        @Body individualGamePostRequestBody: RequestBody
    ): Response<List<IndividualGameDataItem>>
}
