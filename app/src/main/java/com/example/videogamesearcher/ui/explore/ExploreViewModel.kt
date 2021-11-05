package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.*
import com.example.videogamesearcher.R
import com.example.videogamesearcher.models.SearchResultsResponse
import com.example.videogamesearcher.models.TwitchAuthorization
import com.example.videogamesearcher.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ExploreViewModel(private val repository: Repository, private val app: Application, private val resources: Resources) : ViewModel() {

    val authorizationResponse: MutableLiveData<Response<TwitchAuthorization>> = MutableLiveData()
    val twitchAuthorization: MutableLiveData<TwitchAuthorization> = MutableLiveData()
    val gamesListResponse: MutableLiveData<Response<SearchResultsResponse>> = MutableLiveData()
    val gamesListResults: MutableLiveData<SearchResultsResponse> = MutableLiveData()
    val observer = Observer()

    class Observer() : BaseObservable() {
        @Bindable
        val nameSearchText: MutableLiveData<String> = MutableLiveData()
        }

    fun getPlatformStrArray(): Array<String> {
        return resources.getStringArray(R.array.platforms)
    }

    fun getGenreStrArray(): Array<String> {
        return resources.getStringArray(R.array.genre)
    }

    fun getMultiplayerStrArray(): Array<String> {
        return resources.getStringArray(R.array.multiplayer)
    }

    fun getAccessToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getAccessToken()
            authorizationResponse.postValue(response)
        }
    }

    fun searchGames(accessToken: String, gamesSearch: RequestBody) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.searchGames(accessToken, gamesSearch)
            gamesListResponse.postValue(response)
        }
    }

    var searchText: LiveData<RequestBody> = observer.nameSearchText.map { text ->
        "search \"${text}\";\nfields name, genres.name, platforms.name, game_modes.name, cover.url;\nlimit 100;".toRequestBody("text/plain".toMediaTypeOrNull())
    }

}