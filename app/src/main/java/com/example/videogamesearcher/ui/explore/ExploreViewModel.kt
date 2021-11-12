package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.*
import com.example.videogamesearcher.Constants.Companion.GAME_MODES_SPINNER_PROMPT
import com.example.videogamesearcher.Constants.Companion.GENRE_SPINNER_PROMPT
import com.example.videogamesearcher.Constants.Companion.PLATFORM_SPINNER_PROMPT
import com.example.videogamesearcher.models.*
import com.example.videogamesearcher.models.explore_spinners.SpinnerResponse
import com.example.videogamesearcher.models.search_results.SearchResultsResponse
import com.example.videogamesearcher.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ExploreViewModel(private val repository: Repository, private val app: Application, private val resources: Resources) : ViewModel() {
    //Live Data for the Access/Authorization Token
    val twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()

    //Live Data for the Search functionality
    val gamesList: MutableLiveData<SearchResultsResponse> = MutableLiveData()

    //Text to be included with every search
    private val basicSearchText = "\nfields name, genres.name, platforms.name, game_modes.name, cover.url;\nlimit 100;\n"
    private val spinnerBody: RequestBody = "fields name;\nsort name asc;\n limit 500;".toRequestBody("text/plain".toMediaTypeOrNull())

    //Text for filters to be included with searches
    var platformText: MutableLiveData<String> = MutableLiveData("")
    var genreText: MutableLiveData<String> = MutableLiveData("")
    var gameModesText: MutableLiveData<String> = MutableLiveData("")
    val nameSearchText: MutableLiveData<String> = MutableLiveData("")


    fun getAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getAccessToken()
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
            val response = repository.searchGames(accessToken, gamesSearch)
            if (response.isSuccessful) {
                gamesList.postValue(response.body())
            } else {
                //Change how this is handled in the future.
                println("Twitch auth token response not successful.")
            }
        }
    }

    private fun platformsList(): LiveData<SpinnerResponse?> = twitchAuthorization.switchMap { twitchAuthorization ->
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                val response = twitchAuthorization?.let { twitchAuthorization -> repository.getPlatformsList(twitchAuthorization.access_token, spinnerBody) }
                emit(response?.body())
            }
    }

    private fun genresList(): LiveData<SpinnerResponse?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> repository.getGenresList(twitchAuthorization.access_token, spinnerBody) }
            emit(response?.body())
        }
    }

    private fun gameModesList(): LiveData<SpinnerResponse?> = twitchAuthorization.switchMap { twitchAuthorization ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            val response = twitchAuthorization?.let { twitchAuthorization -> repository.getGameModesList(twitchAuthorization.access_token, spinnerBody) }
            emit(response?.body())
        }
    }

    fun createPlatformsList() = platformsList().map { platformList ->
        createSpinnerMutableList(platformList, PLATFORM_SPINNER_PROMPT)
    }

    fun createGenresList() = genresList().map { genresList ->
        createSpinnerMutableList(genresList, GENRE_SPINNER_PROMPT)
    }

    fun createGameModesList() = gameModesList().map { gameModesList ->
        createSpinnerMutableList(gameModesList, GAME_MODES_SPINNER_PROMPT)
    }

    private fun createSpinnerMutableList(spinnerListItem: SpinnerResponse?, spinnerPrompt: String): MutableList<String> {
        val spinnerList: MutableList<String> = emptyList<String>().toMutableList()
        spinnerList.add(spinnerPrompt)
        if (spinnerListItem != null) {
            for (i in spinnerListItem.indices) {
                spinnerList.add(spinnerListItem[i].name)
            }
        }
        return spinnerList
    }

    fun searchText(): LiveData<RequestBody> =
        nameSearchText.switchMap { searchText ->
            platformText.switchMap { platformText ->
                genreText.switchMap { genreText ->
                    gameModesText.map { gameModesText ->
                        val text =
                            basicSearchText +
                            "${if (searchText == "" || searchText == null) {""} else {"search \"${searchText}\";"}}\n" +
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
}