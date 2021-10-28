package com.example.videogamesearcher.repository

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.models.TwitchAccessToken
import com.example.videogamesearcher.api.RetrofitInstance
import retrofit2.Response

class Repository {

    suspend fun pushPostAccess(): Response<TwitchAccessToken> {
        return RetrofitInstance.apiAccessToken.pushPostAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }
}