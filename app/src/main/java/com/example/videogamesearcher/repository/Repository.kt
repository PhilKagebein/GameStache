package com.example.videogamesearcher.repository

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.api.RetrofitInstance
import com.example.videogamesearcher.models.GameNameResponse
import okhttp3.RequestBody
import retrofit2.Response

class Repository {

    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun getGameNameAndPlatform(accessToken: String, gameInfoSearch: RequestBody) :Response<GameNameResponse> {
        return RetrofitInstance.api.getGameNameAndPlatform("Bearer $accessToken", gameInfoSearch)
    }
}
