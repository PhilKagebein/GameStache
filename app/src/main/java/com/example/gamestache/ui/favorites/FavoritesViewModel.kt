package com.example.gamestache.ui.favorites

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.koin.exploreViewModelModule
import com.example.gamestache.models.search_results.*
import com.example.gamestache.repository.GameStacheRepository
import com.example.gamestache.repository.GameStacheRepository.Companion.concatCoverUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI


class FavoritesViewModel(private val gameStacheRepository: GameStacheRepository) : ViewModel() {

    val favoritesListFromDb: MutableLiveData<List<SearchResultsResponseItem?>> = MutableLiveData()

    fun pullFavoritesListFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepository.getFavoritesFromDb(true).let { listOfFavorites ->
                val favoritesMassaged = massageGameCoverUrl(listOfFavorites)
                favoritesListFromDb.postValue(favoritesMassaged)
            }
        }
    }

    fun massageGameCoverUrl(gamesList: List<SearchResultsResponseItem?>): List<SearchResultsResponseItem?> {
        for (game in gamesList) {

            if (game?.cover?.url == null){
                game?.cover?.url = ""
            } else {
                val url = URI(game.cover.url)
                val segments = url.path.split("/")
                val lastSegment = segments[segments.size - 1]
                val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
                game.cover.url = concatCoverUrl(imageHash)
            }

        }

        return gamesList

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


