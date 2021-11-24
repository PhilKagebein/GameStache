package com.example.videogamesearcher.repository

import androidx.lifecycle.LiveData
import com.example.videogamesearcher.Constants.Companion.CLIENT_ID
import com.example.videogamesearcher.Constants.Companion.CLIENT_SECRET
import com.example.videogamesearcher.Constants.Companion.GRANT_TYPE
import com.example.videogamesearcher.api.RetrofitInstance
import com.example.videogamesearcher.models.*
import com.example.videogamesearcher.models.explore_spinners.*
import com.example.videogamesearcher.models.search_results.SearchResultsResponse
import okhttp3.RequestBody
import retrofit2.Response

class ExploreRepository(private val platformsResponseDao: SpinnerResponseDao, private val genresResponseDao: SpinnerResponseDao, private val gameModesResponseDao: SpinnerResponseDao) {

    val readPlatformsList: LiveData<List<PlatformsResponseItem>> = platformsResponseDao.getPlatformsListData()
    val readGenresList: LiveData<List<GenresResponseItem>> = genresResponseDao.getGenresListData()
    val readGameModesList: LiveData<List<GameModesResponseItem>> = gameModesResponseDao.getGameModesListData()

/*    fun getPlatformsList(): LiveData<List<PlatformsResponseItem>> {
       // determine if we need to make the api call
        //if we do, make api call
        //if successful, store in Room
        //
        return platformsResponseDao.getPlatformsListData()
    }*/

    suspend fun addPlatformsListToRoom(spinnerResponseItem: PlatformsResponseItem){
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
