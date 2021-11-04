package com.example.videogamesearcher.ui.explore

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.videogamesearcher.repository.Repository

class ExploreViewModelFactory(private val repository: Repository, private val application: Application, private val resources: Resources): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExploreViewModel(repository, application, resources) as T
    }
}