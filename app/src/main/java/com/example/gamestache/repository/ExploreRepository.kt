package com.example.gamestache.repository

import androidx.lifecycle.LiveData
import com.example.gamestache.Constants.Companion.CLIENT_ID
import com.example.gamestache.Constants.Companion.CLIENT_SECRET
import com.example.gamestache.Constants.Companion.GRANT_TYPE
import com.example.gamestache.room.GameModesSpinnerDao
import com.example.gamestache.room.GenresSpinnerDao
import com.example.gamestache.room.PlatformSpinnerDao
import com.example.gamestache.api.RetrofitInstance
import com.example.gamestache.models.*
import com.example.gamestache.models.explore_spinners.*
import com.example.gamestache.models.search_results.SearchResultsResponse
import okhttp3.RequestBody
import retrofit2.Response

class ExploreRepository(private val platformsResponseDao: PlatformSpinnerDao, private val genresResponseDao: GenresSpinnerDao, private val gameModesResponseDao: GameModesSpinnerDao) {

    val readPlatformsList: LiveData<List<GenericSpinnerItem>> = platformsResponseDao.getPlatformsListData()
    val readGenresList: LiveData<List<GenericSpinnerItem>> = genresResponseDao.getGenresListData()
    val readGameModesListFromRoomDB: LiveData<List<GenericSpinnerItem>> = gameModesResponseDao.getGameModesListData()

    //TODO: ASK KEVIN ABOUT NEVER USING @UPDATE SINCE I WON'T KNOW WHEN A NEW CONSOLE WILL BE RELEASED FOR EXAMPLE. SHOULD ALWAYS JUST BE INSERT YES?

    suspend fun addPlatformsListToRoom(spinnerResponseItem: PlatformsResponseItem) {
        platformsResponseDao.addPlatformsList(spinnerResponseItem)
    }

    suspend fun addGenresListToRoom(spinnerResponseItem: GenresResponseItem){
        genresResponseDao.addGenresList(spinnerResponseItem)
    }

    suspend fun addGameModesListToRoom(spinnerResponseItem: GameModesResponseItem){
        gameModesResponseDao.addGameModesList(spinnerResponseItem)
    }

    suspend fun getAccessToken(): Response<TwitchAuthorization> {
        return RetrofitInstance.apiAccessToken.getAccessToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
    }

    suspend fun searchGames(accessToken: String, gamesSearch: RequestBody): Response<SearchResultsResponse> {
        return RetrofitInstance.api.searchGames("Bearer $accessToken", gamesSearch)
    }

    suspend fun getPlatformsList(accessToken: String, platformsPostRequestBody: RequestBody): Response<List<PlatformsResponseItem>> {
        return RetrofitInstance.api.getPlatformsList("Bearer $accessToken", platformsPostRequestBody)
    }

    suspend fun getGenresList(accessToken: String, genrePostRequestBody: RequestBody): Response<List<GenresResponseItem>> {
        return RetrofitInstance.api.getGenresList("Bearer $accessToken", genrePostRequestBody)
    }

    suspend fun getGameModesList(accessToken: String, gameModesPostRequestBody: RequestBody): Response<List<GameModesResponseItem>> {
        return RetrofitInstance.api.getGameModesList("Bearer $accessToken", gameModesPostRequestBody)
    }

}
