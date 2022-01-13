package com.example.gamestache.ui.favorites

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestache.massageDataForListAdapter
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesViewModel(private val gameStacheRepository: GameStacheRepository) : ViewModel() {

    val favoritesListFromDb: MutableLiveData<List<SearchResultsResponseItem?>> = MutableLiveData()

    fun pullFavoritesListFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepository.getFavoritesFromDb(true).let { listOfFavorites ->
                val favoritesMassaged = massageDataForListAdapter(listOfFavorites)
                favoritesListFromDb.postValue(favoritesMassaged)
            }
        }
    }

    fun setNoFavoritesTextViewVisibility(favoritesList: List<SearchResultsResponseItem?>): Int {
        if (favoritesList.isNullOrEmpty()) return VISIBLE
        else return GONE
    }

    fun setFavoritesListRecyclerViewVisibility(favoritesList: List<SearchResultsResponseItem?>): Int {
        if (favoritesList.isNullOrEmpty()) return GONE
        else return VISIBLE
    }

    fun filterFavorites(filterQuery: String): LiveData<List<SearchResultsResponseItem>> {
        return gameStacheRepository.filterFavoriteGames(filterQuery, true)
    }

}


