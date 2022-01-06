package com.example.gamestache.ui.explore

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.models.*
import com.example.gamestache.models.explore_spinners.*
import com.example.gamestache.models.search_results.*
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

class ExploreViewModel(private val exploreRepository: GameStacheRepository) : ViewModel() {
    //Live Data for the Access/Authorization Token
    val twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()

    //Live Data for the Search functionality
    private val gamesList: MutableLiveData<SearchResultsResponse> = MutableLiveData()

    private val spinnersPostRequestBody: RequestBody = "fields name;\n limit 500;".toRequestBody("text/plain".toMediaTypeOrNull())

    //Text for filters to be included with searches
    var platformText: MutableLiveData<String> = MutableLiveData("")
    var genreText: MutableLiveData<String> = MutableLiveData("")
    var gameModesText: MutableLiveData<String> = MutableLiveData("")
    val nameSearchText: MutableLiveData<String> = MutableLiveData("")
    val progressBarIsVisible: MutableLiveData<Boolean> = MutableLiveData(false)

    val currentPlatformListInDb: LiveData<List<GenericSpinnerItem>> = exploreRepository.getPlatformsListFromDb()
    val currentGenreListInDb: LiveData<List<GenericSpinnerItem>> = exploreRepository.getGenresListFromDb()
    val currentGameModesListInDb: LiveData<List<GenericSpinnerItem>> = exploreRepository.getGameModesListFromDb()

    fun addPlatformsListToRoom(spinnerResponseItem: PlatformsResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.storePlatformsListToDb(spinnerResponseItem)
        }
    }

    fun addGenresListToRoom(spinnerResponseItem: GenresResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.storeGenresListToDb(spinnerResponseItem)
        }
    }

    fun addGameModesListToRoom(spinnerResponseItem: GameModesResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.storeGameModesListToDb(spinnerResponseItem)
        }
    }

    fun getAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = exploreRepository.getAccessToken()
            if (response.isSuccessful){
                twitchAuthorization.postValue(response.body())
            }
            else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
        }
    }

    fun searchGames(accessToken: String, gamesSearch: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            progressBarIsVisible.postValue(true)
            val response = exploreRepository.searchGames(accessToken, gamesSearch)
            if (response.isSuccessful) {
                gamesList.postValue(response.body())
            } else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
            progressBarIsVisible.postValue(false)
        }
    }

    fun transformDataForListAdapter(): LiveData<List<SearchResultsResponseItem>> = gamesList.map{ gamesList ->

        for (i in gamesList.indices) {
            gamesList[i].cover?.url = gamesList[i].cover?.url?.let { getURlHash(it) }.toString()

            if (gamesList[i].platforms.isNullOrEmpty()) {
                continue
            } else {
                for (x in gamesList[i].platforms.indices) {
                    gamesList[i].platforms[x]?.name = getPlatformsText(gamesList, i, x)
                }
            }

            if (gamesList[i].genres.isNullOrEmpty()) {
                continue
            } else {
                for (x in gamesList[i].genres.indices) {
                    gamesList[i].genres[x]?.name = getGenreText(gamesList, i, x)
                }
            }

            if (gamesList[i].game_modes.isNullOrEmpty()) {
                continue
            } else {
                for (x in gamesList[i].game_modes.indices) {
                    gamesList[i].game_modes[x]?.name = getGamesModesText(gamesList, i, x)
                }
            }
       }
        gamesList
    }

    private fun getURlHash(coverURL: String?): String {
        if (coverURL == null){
            return ""
        } else {
            val url = URI(coverURL)
            val segments = url.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            //TODO: MAKE STATIC FUNCTION BELOW
            return "https://images.igdb.com/igdb/image/upload/t_cover_big/${imageHash}.jpg"
        }
    }

    private fun getPlatformsText(gamesList: SearchResultsResponse, i: Int, x: Int): String {
          if (x == ((gamesList[i].platforms.size) - 1)) {
              return gamesList[i].platforms[x]?.name.toString()
          } else {
              return "${gamesList[i].platforms[x]?.name}, "
            }
        }

    private fun getGenreText(gamesList: SearchResultsResponse, i: Int, x: Int): String {
        if (x == ((gamesList[i].genres.size) - 1)) {
            return "${gamesList[i].genres[x]?.name}"
        } else {
            return "${gamesList[i].genres[x]?.name}, "
            }
        }

    private fun getGamesModesText(gamesList: SearchResultsResponse, i: Int, x: Int): String {
        if (x == ((gamesList[i].game_modes.size) - 1)) {
            return "${gamesList[i].game_modes[x]?.name}"
        } else {
            return "${gamesList[i].game_modes[x]?.name}, "
            }
        }

    //TODO: CHANGE THIS NAME
    fun getPlatformsListFromRoom(): LiveData<List<PlatformsResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getPlatformsListFromDb(twitchAuthorization.access_token, spinnersPostRequestBody) }
            emit(response?.body())
            }
    }

    fun genresResponse(): LiveData<List<GenresResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getGenresListFromDb(twitchAuthorization.access_token, spinnersPostRequestBody) }
            emit(response?.body())
        }
    }

    fun gameModesResponse(): LiveData<List<GameModesResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getGameModesListFromDb(twitchAuthorization.access_token, spinnersPostRequestBody) }
            emit(response?.body())
        }
    }

    //TODO: TALK TO KEVIN ABOUT WHY I MADE GENERICSPINNERITEM CLASS AND THIS SOLUTION TO THE PROBLEM
    fun addPromptToSpinnerList(spinnerList: List<GenericSpinnerItem>?, spinnerPrompt: String): MutableList<String?> {
        val list: MutableList<String?> = emptyList<String>().toMutableList()
        list.add(spinnerPrompt)
        if (spinnerList != null) {
            for (i in spinnerList.indices) {
                list.add(spinnerList[i].name)
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

    //TODO SHOW KEVIN THIS AND SEE IF HE THINKS THIS IS THE BEST IT WILL BE
    private fun makeWhereClause(spinnerMap: Map<String,String>): String {
        var whereClause = "where "
        val filteredMap = spinnerMap.filterNot { it.value.isEmpty() }
        val mapList = filteredMap.toList()
        val mapSize = filteredMap.size
        var currentMapItem = 1

        if (mapSize == 0) {
            return ""
        } else {
            for (i in mapList) {
                if (currentMapItem < mapSize) {
                    whereClause += "${i.first} = ${i.second} & "
                    currentMapItem++
                } else if (currentMapItem == mapSize){
                    whereClause += "${i.first} = ${i.second};"
                }
            }
        }
        return whereClause
    }

    fun setExploreSpinnersClearButtonVisibility(itemPosition: Int): Int {
        if (itemPosition == 0) return GONE
        else return VISIBLE
    }

    fun clearEditTextField(observedFieldValue: String): Int {
        if (!observedFieldValue.isNullOrEmpty() || observedFieldValue.isNotBlank()) {
            return VISIBLE
        } else {
            return GONE
        }
    }
    companion object {
        const val BASIC_SEARCH_TEXT = "\nfields name, genres.name, platforms.name, game_modes.name, cover.url;\nlimit 100;"
    }
}
