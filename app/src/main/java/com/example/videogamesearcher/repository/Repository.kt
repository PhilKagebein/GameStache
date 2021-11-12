package com.example.videogamesearcher.repository

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.api.RetrofitInstance
import com.example.videogamesearcher.models.*
import com.example.videogamesearcher.models.explore_spinners.SpinnerResponse
import com.example.videogamesearcher.models.search_results.SearchResultsResponse
import okhttp3.RequestBody
import retrofit2.Response

class Repository {

    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun searchGames(accessToken: String, gamesSearch: RequestBody): Response<SearchResultsResponse> {
        return RetrofitInstance.api.searchGames("Bearer $accessToken", gamesSearch)
    }

    suspend fun getPlatformsList(accessToken: String, platformsBody: RequestBody): Response<SpinnerResponse> {
        return RetrofitInstance.api.getPlatformsList("Bearer $accessToken", platformsBody)
    }

    suspend fun getGenresList(accessToken: String, platformsBody: RequestBody): Response<SpinnerResponse> {
        return RetrofitInstance.api.getGenresList("Bearer $accessToken", platformsBody)
    }

    suspend fun getGameModesList(accessToken: String, platformsBody: RequestBody): Response<SpinnerResponse> {
        return RetrofitInstance.api.getGameModesList("Bearer $accessToken", platformsBody)
    }
}
