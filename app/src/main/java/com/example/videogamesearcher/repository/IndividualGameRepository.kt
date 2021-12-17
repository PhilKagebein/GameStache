package com.example.videogamesearcher.repository

import androidx.lifecycle.LiveData
import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.api.RetrofitInstance
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.models.individual_game.IndividualGameDataItem
import com.example.videogamesearcher.ui.individual_game.IndividualGameDao
import okhttp3.RequestBody
import retrofit2.Response

class IndividualGameRepository(private val individualGameDao: IndividualGameDao) {

    fun checkIfGameIsInRoom(gameID: Int): LiveData<Int> = individualGameDao.checkIfGameExistsInRoom(gameID)
    fun getIndividualGameDataFromRoom(gameID: Int): LiveData<IndividualGameDataItem?> = individualGameDao.getIndividualGameDataFromRoom(gameID)

    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun getIndividualGameData(accessToken: String, individualGameSearch: RequestBody): Response<IndividualGameDataItem> {
        return RetrofitInstance.api.getIndividualGameData("Bearer $accessToken", individualGameSearch)
    }

    suspend fun storeIndividualGameToRoom(individualGameData: IndividualGameDataItem) {
        individualGameDao.storeIndividualGameDataInRoom(individualGameData)
    }

}