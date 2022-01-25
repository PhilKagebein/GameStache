package com.example.gamestache.repository

import androidx.lifecycle.LiveData
import com.example.gamestache.Constants.Companion.CLIENT_ID
import com.example.gamestache.Constants.Companion.CLIENT_SECRET
import com.example.gamestache.Constants.Companion.GRANT_TYPE
import com.example.gamestache.api.TwitchApi
import com.example.gamestache.api.TwitchApiAuth
import com.example.gamestache.models.TwitchAuthorization
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem
import com.example.gamestache.models.individual_game.IndividualGameDataItem
import com.example.gamestache.models.search_results.SearchResultsResponse
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.room.*
import okhttp3.RequestBody
import retrofit2.Response
import java.time.LocalDateTime

class GameStacheRepository(
    private val api: TwitchApi,
    private val authApi: TwitchApiAuth,
    private val individualGameDao: IndividualGameDao,
    private val platformsResponseDao: PlatformSpinnerDao,
    private val genresResponseDao: GenresSpinnerDao,
    private val gameModesResponseDao: GameModesSpinnerDao,
    private val twitchAuthorizationDao: TwitchAuthorizationDao,
    private val favoritesAndWishlistDao: FavoritesAndWishlistDao
) {

    fun getPlatformsListFromDb(): LiveData<MutableList<GenericSpinnerItem>> = platformsResponseDao.getPlatformsListFromDb()
    fun getGenresListFromDb(): LiveData<MutableList<GenericSpinnerItem>> = genresResponseDao.getGenresListFromDb()
    fun getGameModesListFromDb(): LiveData<MutableList<GenericSpinnerItem>> = gameModesResponseDao.getGameModesListFromDb()
    fun checkIfGameIsInDb(gameID: Int): LiveData<Int> = individualGameDao.checkIfGameExistsInRoom(gameID)
    fun getIndividualGameDataFromDb(gameID: Int): LiveData<List<IndividualGameDataItem?>> = individualGameDao.getIndividualGameDataFromRoom(gameID)
    private fun getTwitchAuthFromDb(): TwitchAuthorization = twitchAuthorizationDao.getTwitchAuthFromDb()
    private fun getAuthStatusInDb(): Int = twitchAuthorizationDao.checkIfAuthTokenIsInDB()
    suspend fun checkIfGameIsInFavoriteAndWishlistTable(gameId: Int): Int = favoritesAndWishlistDao.checkIfGameIsInFavoriteAndWishlistTable(gameId)
    fun getFavoritesFromDb(favoriteStatus: Boolean): List<SearchResultsResponseItem?> = favoritesAndWishlistDao.getFavoritesFromDb(favoriteStatus)
    fun getWishlistFromDb(wishlistStatus: Boolean): List<SearchResultsResponseItem?> = favoritesAndWishlistDao.getWishlistFromDb(wishlistStatus)
    fun filterFavoriteGames(filterQuery: String, favoriteStatus: Boolean): LiveData<List<SearchResultsResponseItem>> = favoritesAndWishlistDao.filterFavoriteGames(filterQuery, favoriteStatus)
    fun filterWishlist(filterQuery: String, wishlistStatus: Boolean): LiveData<List<SearchResultsResponseItem>> = favoritesAndWishlistDao.filterWishlist(filterQuery, wishlistStatus)
    fun getIndividualGameInfo(gameId: Int): SearchResultsResponseItem = favoritesAndWishlistDao.getIndividualGameInfo(gameId)

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

    private suspend fun storeTwitchAuthInDb(twitchAuthorization: TwitchAuthorization) {
        twitchAuthorizationDao.addTwitchAuthToDb(twitchAuthorization)
    }

    suspend fun addGameToFavoriteAndWishlistTable(game: SearchResultsResponseItem?) {
        favoritesAndWishlistDao.addGameToFavoriteAndWishlistTable(game)
    }

    suspend fun updateFavoriteAndWishlistStatus(gameId: Int, favoriteStatus: Boolean, wishlistStatus: Boolean) {
        favoritesAndWishlistDao.updateFavoriteAndWishlistStatus(gameId, favoriteStatus, wishlistStatus)
    }

    private suspend fun getNewAuthToken(): TwitchAuthorization? {
        val twitchAuth = authApi.getAuthToken(CLIENT_ID, CLIENT_SECRET, GRANT_TYPE)
        if (twitchAuth.isSuccessful) {
            twitchAuth.body()?.token_birth_date = LocalDateTime.now()
            twitchAuth.body()?.let { storeTwitchAuthInDb(it) }
            return twitchAuth.body()
        } else {
            return null
        }
    }

    private fun determineTokenExpirationDate(authToken: TwitchAuthorization): LocalDateTime {
        val authTokenBirthDate = authToken.token_birth_date
        return authTokenBirthDate.plusDays(DAYS_TO_GET_EXPIRATION)
    }

    suspend fun getAuthToken(): TwitchAuthorization? {
        val authStatusInDb = getAuthStatusInDb()

        if (authStatusInDb == AUTH_NOT_IN_DB) {
            return getNewAuthToken()
        } else {
            val authToken = getTwitchAuthFromDb()
            val tokenExpirationDate = determineTokenExpirationDate(authToken)

            if (LocalDateTime.now().isAfter(tokenExpirationDate)) {
                return getNewAuthToken()
            } else {
                return authToken
            }
        }
    }

     suspend fun searchForGames(authToken: String, gamesSearch: RequestBody): Response<SearchResultsResponse> {
        return api.searchForGames("Bearer $authToken", gamesSearch)
    }

     suspend fun getPlatformsListFromApi(authToken: String, platformsPostRequestBody: RequestBody): Response<List<PlatformsResponseItem>> {
        return api.getPlatformsList("Bearer $authToken", platformsPostRequestBody)
    }

     suspend fun getGenresListFromApi(authToken: String, genrePostRequestBody: RequestBody): Response<List<GenresResponseItem>> {
        return api.getGenresList("Bearer $authToken", genrePostRequestBody)
    }

     suspend fun getGameModesListFromApi(authToken: String, gameModesPostRequestBody: RequestBody): Response<List<GameModesResponseItem>> {
        return api.getGameModesList("Bearer $authToken", gameModesPostRequestBody)
    }

    suspend fun getIndividualGameDataFromApi(authToken: String, individualGameSearch: RequestBody): Response<List<IndividualGameDataItem>> {
        return api.getIndividualGameData("Bearer $authToken", individualGameSearch)
    }

    companion object {
        const val AUTH_NOT_IN_DB = 0
        const val DAYS_TO_GET_EXPIRATION: Long = 30

    }

}
