package com.example.videogamesearcher.repository

import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.api.RetrofitInstance
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.models.individual_game.IndividualGameData
import okhttp3.RequestBody
import retrofit2.Response

class IndividualGameRepository {

    //@@@ktg repopstories can probably be combined into one "GameInfoRepository" since they are all
    // calling into the same service.
    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun getIndividualGameData(accessToken: String, individualGameSearch: RequestBody): Response<IndividualGameData> {
        return RetrofitInstance.api.getIndividualGameData("Bearer $accessToken", individualGameSearch)
    }
}