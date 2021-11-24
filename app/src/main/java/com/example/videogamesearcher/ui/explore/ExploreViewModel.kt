package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.videogamesearcher.R
import com.example.videogamesearcher.models.*
import com.example.videogamesearcher.models.explore_spinners.*
import com.example.videogamesearcher.models.search_results.*
import com.example.videogamesearcher.repository.ExploreRepository
import com.example.videogamesearcher.ui.explore.ExploreFragment.Companion.BASIC_SEARCH_TEXT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

class ExploreViewModel(private val app: Application, private val resources: Resources) : ViewModel() {
    //Live Data for the Access/Authorization Token
    val twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()

    //Live Data for the Search functionality
    val gamesList: MutableLiveData<SearchResultsResponse> = MutableLiveData()

    private val spinnersPostRequestBody: RequestBody = "fields name;\n limit 500;".toRequestBody("text/plain".toMediaTypeOrNull())

    //Text for filters to be included with searches
    var platformText: MutableLiveData<String> = MutableLiveData("")
    var genreText: MutableLiveData<String> = MutableLiveData("")
    var gameModesText: MutableLiveData<String> = MutableLiveData("")
    val nameSearchText: MutableLiveData<String> = MutableLiveData("")

    val readPlatformsList: LiveData<List<PlatformsResponseItem>>
    val readGenresList: LiveData<List<GenresResponseItem>>
    val readGameModesList: LiveData<List<GameModesResponseItem>>

    private val exploreRepository: ExploreRepository

    init {
        val platformsListDao = SpinnerResponseDatabase.getPlatformsListDatabase(app).spinnerResponseDao()
        val genresListDao = SpinnerResponseDatabase.getGenresListDatabase(app).spinnerResponseDao()
        val gameModesDao = SpinnerResponseDatabase.getGameModesListDatabase(app).spinnerResponseDao()
        exploreRepository = ExploreRepository(platformsListDao, genresListDao, gameModesDao)
        readPlatformsList = exploreRepository.readPlatformsList
        readGenresList = exploreRepository.readGenresList
        readGameModesList = exploreRepository.readGameModesList
    }

    fun addPlatformsListToRoom(spinnerResponseItem: PlatformsResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.addPlatformsListToRoom(spinnerResponseItem)
        }
    }

    fun addGenresListToRoom(spinnerResponseItem: GenresResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.addGenresListToRoom(spinnerResponseItem)
        }
    }

    fun addGameModesListToRoom(spinnerResponseItem: GameModesResponseItem){
        viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.addGameModesListToRoom(spinnerResponseItem)
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
            val response = exploreRepository.searchGames(accessToken, gamesSearch)
            if (response.isSuccessful) {
                gamesList.postValue(response.body())
            } else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
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

    //ask Kevin how to handle emit() if response isn't successful
    fun platformsResponse(): LiveData<List<PlatformsResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getPlatformsList(twitchAuthorization.access_token, spinnersPostRequestBody) }
                emit(response?.body())
            }
    }

    fun genresResponse(): LiveData<List<GenresResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getGenresList(twitchAuthorization.access_token, spinnersPostRequestBody) }
            emit(response?.body())
        }
    }

    fun gameModesResponse(): LiveData<List<GameModesResponseItem>?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> exploreRepository.getGameModesList(twitchAuthorization.access_token, spinnersPostRequestBody) }
            emit(response?.body())
        }
    }

    fun createPlatformsListFromRoom(spinnerList: List<PlatformsResponseItem>?): MutableList<String> {
        val list: MutableList<String> = emptyList<String>().toMutableList()
        list.add(resources.getString(R.string.platform_spinner_prompt))
        if (spinnerList != null) {
            for (i in spinnerList.indices) {
                list.add(spinnerList[i].name)
            }
        }
        return list
    }

    fun createGenresListFromRoom(spinnerList: List<GenresResponseItem>?): MutableList<String> {
        val list: MutableList<String> = emptyList<String>().toMutableList()
        list.add(resources.getString(R.string.genre_spinner_prompt))
        if (spinnerList != null) {
            for (i in spinnerList.indices) {
                list.add(spinnerList[i].name)
            }
        }
        return list
    }

    fun createGameModesListFromRoom(spinnerList: List<GameModesResponseItem>?): MutableList<String> {
        val list: MutableList<String> = emptyList<String>().toMutableList()
        list.add(resources.getString(R.string.game_modes_spinner_prompt))
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
                                   /* makeWhereClause(mapOf(
                                        "platform.name" to platformText,

                                    ))*/
                            if (platformText == "" && genreText == "" && gameModesText == "") {""}
                            else if((platformText == "" && genreText == "") || (platformText == "" && gameModesText == "") || (genreText == "" && gameModesText == "")){"where $platformText$genreText$gameModesText;"}
                            else if(platformText == "") {"where $genreText & $gameModesText;"}
                            else if(genreText == "") {"where $platformText & $gameModesText;"}
                            else if(gameModesText == "") {"where $platformText & $genreText;"}
                            else {"where $platformText & $genreText & $gameModesText;"}
                        text.toRequestBody("text/plain".toMediaTypeOrNull())
                    }
                }
            }
        }

    //FINISH THIS LATER
 /*   fun makeWhereClause(temp: Map<String,String>): String {

    }*/

    fun setExploreSpinnersClearButtonVisibility(itemPosition: Int): Int {
        if (itemPosition == 0) return GONE
        else return VISIBLE
    }

}
