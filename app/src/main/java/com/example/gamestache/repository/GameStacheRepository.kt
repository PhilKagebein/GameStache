package com.example.gamestache.repository

import androidx.lifecycle.LiveData
import com.example.gamestache.Constants.Companion.CLIENT_ID
import com.example.gamestache.Constants.Companion.CLIENT_SECRET
import com.example.gamestache.Constants.Companion.GRANT_TYPE
import com.example.gamestache.room.GameModesSpinnerDao
import com.example.gamestache.room.GenresSpinnerDao
import com.example.gamestache.room.PlatformSpinnerDao
import com.example.gamestache.api.TwitchApi
import com.example.gamestache.api.TwitchApiAuth
import com.example.gamestache.models.*
import com.example.gamestache.models.explore_spinners.*
import com.example.gamestache.models.individual_game.IndividualGameDataItem
import com.example.gamestache.models.search_results.SearchResultsResponse
import com.example.gamestache.room.IndividualGameDao
import okhttp3.RequestBody
import retrofit2.Response

class GameStacheRepository(private val api: TwitchApi, private val authApi: TwitchApiAuth, private val individualGameDao: IndividualGameDao, private val platformsResponseDao: PlatformSpinnerDao, private val genresResponseDao: GenresSpinnerDao, private val gameModesResponseDao: GameModesSpinnerDao){

    fun getPlatformsList(): LiveData<List<GenericSpinnerItem>> = platformsResponseDao.getPlatformsListFromDb()
    fun getGenresList(): LiveData<List<GenericSpinnerItem>> = genresResponseDao.getGenresListFromDb()
    fun getGameModesList(): LiveData<List<GenericSpinnerItem>> = gameModesResponseDao.getGameModesListFromDb()
    fun checkIfGameIsInRoom(gameID: Int): LiveData<Int> = individualGameDao.checkIfGameExistsInRoom(gameID)
    fun getIndividualGameDataFromRoom(gameID: Int): LiveData<List<IndividualGameDataItem?>> = individualGameDao.getIndividualGameDataFromRoom(gameID)

     suspend fun storePlatformsListToDb(spinnerResponseItem: PlatformsResponseItem) {
        platformsResponseDao.addPlatformsListToDb(spinnerResponseItem)
    }

     suspend fun storeGenresListToDb(spinnerResponseItem: GenresResponseItem){
        genresResponseDao.addGenresListToDb(spinnerResponseItem)
    }

     suspend fun storeGameModesListToDb(spinnerResponseItem: GameModesResponseItem){
        gameModesResponseDao.addGameModesListToDb(spinnerResponseItem)
    }

    suspend fun storeIndividualGameToDb(individualGameData: List<IndividualGameDataItem>) {
        individualGameDao.storeIndividualGameDataInRoom(individualGameData)
    }

     suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return authApi.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

     suspend fun searchGames(accessToken: String, gamesSearch: RequestBody): Response<SearchResultsResponse> {
        return api.searchGames("Bearer $accessToken", gamesSearch)
    }

     suspend fun getPlatformsList(accessToken: String, platformsPostRequestBody: RequestBody): Response<List<PlatformsResponseItem>> {
        return api.getPlatformsList("Bearer $accessToken", platformsPostRequestBody)
    }

     suspend fun getGenresList(accessToken: String, genrePostRequestBody: RequestBody): Response<List<GenresResponseItem>> {
        return api.getGenresList("Bearer $accessToken", genrePostRequestBody)
    }

     suspend fun getGameModesList(accessToken: String, gameModesPostRequestBody: RequestBody): Response<List<GameModesResponseItem>> {
        return api.getGameModesList("Bearer $accessToken", gameModesPostRequestBody)
    }

    suspend fun getIndividualGameData(accessToken: String, individualGameSearch: RequestBody): Response<List<IndividualGameDataItem>> {
        return api.getIndividualGameData("Bearer $accessToken", individualGameSearch)
    }

}
