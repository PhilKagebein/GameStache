package com.example.videogamesearcher.ui.individual_game

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IndividualGameViewModelFactory(private val resources: Resources): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IndividualGameViewModel(resources) as T
    }
}