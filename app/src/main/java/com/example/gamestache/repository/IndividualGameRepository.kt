package com.example.gamestache.repository

import androidx.lifecycle.LiveData
import com.example.gamestache.Constants.Companion.CLIENT_ID
import com.example.gamestache.Constants.Companion.CLIENT_SECRET
import com.example.gamestache.Constants.Companion.GRANT_TYPE
import com.example.gamestache.api.RetrofitInstance
import com.example.gamestache.models.TwitchAuthorization
import com.example.gamestache.models.individual_game.IndividualGameDataItem
import com.example.gamestache.room.IndividualGameDao
import okhttp3.RequestBody
import retrofit2.Response

class IndividualGameRepository(private val individualGameDao: IndividualGameDao) {

    fun checkIfGameIsInRoom(gameID: Int): LiveData<Int> = individualGameDao.checkIfGameExistsInRoom(gameID)
    fun getIndividualGameDataFromRoom(gameID: Int): LiveData<List<IndividualGameDataItem?>> = individualGameDao.getIndividualGameDataFromRoom(gameID)

    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun getIndividualGameData(accessToken: String, individualGameSearch: RequestBody): Response<List<IndividualGameDataItem>> {
        return RetrofitInstance.api.getIndividualGameData("Bearer $accessToken", individualGameSearch)
    }

    suspend fun storeIndividualGameToRoom(individualGameData: List<IndividualGameDataItem>) {
        individualGameDao.storeIndividualGameDataInRoom(individualGameData)
    }

}