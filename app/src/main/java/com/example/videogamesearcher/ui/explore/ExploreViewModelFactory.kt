package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExploreViewModelFactory(private val application: Application, private val resources: Resources): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ExploreViewModel(application, resources) as T
    }
}