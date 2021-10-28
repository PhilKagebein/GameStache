package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videogamesearcher.R
import com.example.videogamesearcher.models.TwitchAccessToken
import com.example.videogamesearcher.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class ExploreViewModel(private val repository: Repository, private val app: Application, private val resources: Resources) : ViewModel() {

    val accessTokenResponse: MutableLiveData<Response<TwitchAccessToken>> = MutableLiveData()

    fun getPlatformStrArray(): Array<String> {
        return resources.getStringArray(R.array.platforms)
    }

    fun getGenreStrArray(): Array<String> {
        return resources.getStringArray(R.array.genre)
    }

    fun getMultiplayerStrArray(): Array<String> {
        return resources.getStringArray(R.array.multiplayer)
    }

    fun pushPostAccess() {
        viewModelScope.launch {
            val response = repository.pushPostAccess()
            accessTokenResponse.postValue(response)
        }
    }
}