package com.example.gamestache.ui.explore

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.isOnline
import com.example.gamestache.makeNoInternetToast
import com.example.gamestache.massageDataForListAdapter
import com.example.gamestache.models.TwitchAuthorization
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem
import com.example.gamestache.models.search_results.SearchResultsResponse
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.repository.GameStacheRepository
import com.example.gamestache.ui.explore.ExploreFragment.Companion.IS_OFFLINE_LOG_TEXT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ExploreViewModel(private val gameStacheRepo: GameStacheRepository) : ViewModel() {
    val twitchAuthorization: MutableLiveData<TwitchAuthorization?> = MutableLiveData()

    private val gamesList: MutableLiveData<SearchResultsResponse> = MutableLiveData()

    private val spinnersPostRequestBody: RequestBody = "fields name;\n limit 500;".toRequestBody("text/plain".toMediaTypeOrNull())

    var platformText: MutableLiveData<String> = MutableLiveData("")
    var genreText: MutableLiveData<String> = MutableLiveData("")
    var gameModesText: MutableLiveData<String> = MutableLiveData("")
    val nameSearchText: MutableLiveData<String> = MutableLiveData("")
    val progressBarIsVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    val currentPlatformListInDb: LiveData<List<GenericSpinnerItem>> = gameStacheRepo.getPlatformsListFromDb()
    val currentGenreListInDb: LiveData<List<GenericSpinnerItem>> = gameStacheRepo.getGenresListFromDb()
    val currentGameModesListInDb: LiveData<List<GenericSpinnerItem>> = gameStacheRepo.getGameModesListFromDb()

    var platformSpinnerSelection = 0
    var genreSpinnerSelection = 0
    var gameModesSpinnerSelection = 0

    private fun addPlatformsListToRoom(spinnerResponseItem: PlatformsResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepo.storePlatformsListToDb(spinnerResponseItem)
        }
    }

    private fun addGenresListToRoom(spinnerResponseItem: GenresResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepo.storeGenresListToDb(spinnerResponseItem)
        }
    }

    private fun addGameModesListToRoom(spinnerResponseItem: GameModesResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepo.storeGameModesListToDb(spinnerResponseItem)
        }
    }

    fun getAuthToken(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val authToken = com.example.gamestache.getAuthToken(context, gameStacheRepo)
            twitchAuthorization.postValue(authToken)
        }
    }

    fun searchForGames(accessToken: String, gamesSearch: RequestBody, context: Context, resources: Resources) {
        if (isOnline(context)) {
            viewModelScope.launch(Dispatchers.IO) {
                progressBarIsVisible.postValue(true)
                val response = gameStacheRepo.searchForGames(accessToken, gamesSearch)
                if (response.isSuccessful) {
                    gamesList.postValue(response.body())
                } else {
                    Log.i(GAMES_SEARCH_LOG_TAG, response.toString())
                }
                progressBarIsVisible.postValue(false)
            }
        } else {
            Log.i(GAMES_SEARCH_LOG_TAG, IS_OFFLINE_LOG_TEXT)
            makeNoInternetToast(context, resources).show()
        }
    }

    fun transformDataForListAdapter(): LiveData<List<SearchResultsResponseItem?>> = gamesList.map{ gamesList ->
        massageDataForListAdapter(gamesList)
    }

    fun updateSpinnerListsFromApi(twitchAuthorization: TwitchAuthorization?) {
        viewModelScope.launch(Dispatchers.IO) {
          twitchAuthorization?.let{ twitchAuth ->
                val platformsListFromApi = gameStacheRepo.getPlatformsListFromApi(twitchAuth.access_token, spinnersPostRequestBody)
                val genresListFromApi = gameStacheRepo.getGenresListFromApi(twitchAuth.access_token, spinnersPostRequestBody)
                val gameModesListFromApi = gameStacheRepo.getGameModesListFromApi(twitchAuth.access_token, spinnersPostRequestBody)

              updateSpinnersListsInRoom(platformsListFromApi, genresListFromApi, gameModesListFromApi)
            }
        }
    }

    private fun updateSpinnersListsInRoom(platformsList: Response<List<PlatformsResponseItem>>?, genresList: Response<List<GenresResponseItem>>?, gameModesList: Response<List<GameModesResponseItem>>?) {
        platformsList?.body()?.let { platforms ->
            for (platform in platforms) {
                val platformItem = PlatformsResponseItem(platform.id, platform.name)
                addPlatformsListToRoom(platformItem)
            }
        }

        genresList?.body()?.let { genres ->
            for (genre in genres) {
                val genreItem = GenresResponseItem(genre.id, genre.name)
                addGenresListToRoom(genreItem)
            }
        }

        gameModesList?.body()?.let { gameModes ->
            for (gameMode in gameModes) {
                val gameModeItem = GameModesResponseItem(gameMode.id, gameMode.name)
                addGameModesListToRoom(gameModeItem)
            }
        }
    }

    fun addPromptToSpinnerList(spinnerList: List<GenericSpinnerItem>?, spinnerPrompt: String): MutableList<String?> {
        val list: MutableList<String?> = emptyList<String>().toMutableList()
        list.add(spinnerPrompt)
        if (spinnerList != null) {
            for (item in spinnerList) {
                list.add(item.name)
            }
        }
        return list
    }

    fun searchText(): LiveData<RequestBody> =
        nameSearchText.switchMap { searchText ->
            platformText.switchMap { platformText ->
                genreText.switchMap { genreText ->
                    gameModesText.map { gameModesText ->
                        val text =
                            BASIC_SEARCH_TEXT +
                            "${if (searchText.isNullOrBlank()) {""} else {"search \"${searchText}\";"}}\n" +
                                    makeWhereClause(mapOf(
                                        "platforms.name" to platformText,
                                        "genres.name" to genreText,
                                        "game_modes.name" to gameModesText
                                    ))
                        text.toRequestBody("text/plain".toMediaTypeOrNull())
                    }
                }
            }
        }

    private fun makeWhereClause(spinnerMap: Map<String, String>): String {
        val stringsToJoin = mutableListOf<String>()
        val filteredMap = spinnerMap.filterNot { it.value.isEmpty() }
        if (filteredMap.isEmpty()) { return "" }
        else {
            val mapList = filteredMap.toList()
            for (i in mapList) {
                stringsToJoin.add("${i.first} = ${i.second}")
            }

            return stringsToJoin.joinToString(separator = " & ", prefix = "where ", postfix = ";")
        }
    }

    fun setExploreSpinnersClearButtonVisibility(itemPosition: Int): Int {
        if (itemPosition == 0) return GONE
        else return VISIBLE
    }

    fun clearEditTextField(observedFieldValue: String): Int {
        if (observedFieldValue.isNotEmpty() || observedFieldValue.isNotBlank()) {
            return VISIBLE
        } else {
            return GONE
        }
    }
    companion object {
        const val BASIC_SEARCH_TEXT = "\nfields name, genres.name, platforms.name, game_modes.name, cover.url;\nlimit 100;"
        const val GAMES_SEARCH_LOG_TAG = "ExploreViewModel - Games Search"
    }
}
