package com.example.gamestache.ui.individual_game

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IndividualGameViewModelFactory(private val resources: Resources, private val app: Application): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IndividualGameViewModel(resources, app) as T
    }
}