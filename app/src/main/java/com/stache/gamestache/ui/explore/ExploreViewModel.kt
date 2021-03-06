package com.stache.gamestache.ui.explore

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.stache.gamestache.R
import com.stache.gamestache.isOnline
import com.stache.gamestache.makeNoInternetToast
import com.stache.gamestache.massageDataForListAdapter
import com.stache.gamestache.models.TwitchAuthorization
import com.stache.gamestache.models.explore_spinners.GameModesResponseItem
import com.stache.gamestache.models.explore_spinners.GenericSpinnerItem
import com.stache.gamestache.models.explore_spinners.GenresResponseItem
import com.stache.gamestache.models.explore_spinners.PlatformsResponseItem
import com.stache.gamestache.models.search_results.SearchResultsResponse
import com.stache.gamestache.models.search_results.SearchResultsResponseItem
import com.stache.gamestache.repository.GameStacheRepository
import com.stache.gamestache.ui.explore.ExploreFragment.Companion.IS_OFFLINE_LOG_TEXT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ExploreViewModel(private val gameStacheRepo: GameStacheRepository, private val resources: Resources) : ViewModel() {
    val twitchAuthorization: MutableLiveData<TwitchAuthorization?> = MutableLiveData()

    private val gamesList: MutableLiveData<SearchResultsResponse> = MutableLiveData()

    private val spinnersPostRequestBody: RequestBody = "fields name;\n limit 500;".toRequestBody("text/plain".toMediaTypeOrNull())

    var platformText: MutableLiveData<String> = MutableLiveData("")
    var genreText: MutableLiveData<String> = MutableLiveData("")
    var gameModesText: MutableLiveData<String> = MutableLiveData("")
    val nameSearchText: MutableLiveData<String> = MutableLiveData("")
    val progressBarIsVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    val currentPlatformListInDb: LiveData<MutableList<GenericSpinnerItem>> = gameStacheRepo.getPlatformsListFromDb()
    val currentGenreListInDb: LiveData<MutableList<GenericSpinnerItem>> = gameStacheRepo.getGenresListFromDb()
    val currentGameModesListInDb: LiveData<MutableList<GenericSpinnerItem>> = gameStacheRepo.getGameModesListFromDb()

    var platformSpinnerSelection = 0
    var genreSpinnerSelection = 0
    var gameModesSpinnerSelection = 0

    val isExploreFragmentLoading = MutableStateFlow(true)

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
            val authToken = com.stache.gamestache.getAuthToken(context, gameStacheRepo)
            twitchAuthorization.postValue(authToken)
        }
    }

    fun searchForGames(authToken: String, gamesSearch: RequestBody, context: Context) {
        if (isOnline(context)) {
            viewModelScope.launch(Dispatchers.IO) {
                progressBarIsVisible.postValue(true)
                val response = gameStacheRepo.searchForGames(authToken, gamesSearch)
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

    fun addPromptToSpinnerList(spinnerList: MutableList<GenericSpinnerItem>?, spinnerPrompt: String, spinnerType: ExploreFragment.ExploreSpinners): MutableList<String?> {
        var spinnerItems = spinnerList
        val list: MutableList<String?> = mutableListOf()

        if (spinnerType == ExploreFragment.ExploreSpinners.PLATFORM_SPINNER) {
            spinnerItems = putCurrentGenPlatformsAtBeginningOfList(spinnerItems)
        }
        list.add(spinnerPrompt)

        spinnerItems?.let { items ->
            for (item in items) {
                list.add(item.name)
            }
        }
        return list
    }

    private fun putCurrentGenPlatformsAtBeginningOfList(platformList: MutableList<GenericSpinnerItem>?): MutableList<GenericSpinnerItem>? {
        val currentGenPlatforms = mutableListOf<GenericSpinnerItem>()
        platformList?.let {
        val platformListIterator = platformList.iterator()

            while (platformListIterator.hasNext()) {
                val platform = platformListIterator.next()

                for (id in CURRENT_GEN_PLATFORMS) {
                    if (platform.id == id) {
                        currentGenPlatforms.add(platform)
                        platformListIterator.remove()
                    }
                }

            }
        }

        platformList?.addAll(0, currentGenPlatforms)

        return platformList
    }

    fun searchText(): LiveData<RequestBody> =
        nameSearchText.switchMap { searchQuery ->
            platformText.switchMap { platformText ->
                genreText.switchMap { genreText ->
                    gameModesText.map { gameModesText ->
                        val text =
                            BASIC_SEARCH_TEXT +
                            "${if (searchQuery.isNullOrBlank()) {resources.getString(R.string.empty)} else { makeSearchString(searchQuery) }}\n" +
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
        val CURRENT_GEN_PLATFORMS = listOf(34, 39, 3, 14, 130, 6, 167, 169)
        fun makeSearchString(searchQuery: String) = "search \"${searchQuery}\";"
    }
}

