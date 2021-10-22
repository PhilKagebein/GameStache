package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.videogamesearcher.R

class ExploreViewModel(private val app: Application, private val resources: Resources) : ViewModel() {

    fun displaySearchToast(){
        Toast.makeText(app, "Search worked", Toast.LENGTH_LONG).show()
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
}