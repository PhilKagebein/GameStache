package com.example.gamestache.ui.favorites

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
import com.example.gamestache.models.search_results.*
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI


class FavoritesViewModel(private val gameStacheRepository: GameStacheRepository) : ViewModel() {

    val favoritesListFromDb: MutableLiveData<List<SearchResultsResponseItem?>> = MutableLiveData()

    fun pullFavoritesListFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepository.getFavoritesFromDb().let { listOfFavorites ->
                for (game in listOfFavorites) {
                    game?.cover?.url = createURL(game?.cover?.url)
                }
                favoritesListFromDb.postValue(listOfFavorites)
            }
        }
    }

    private fun createURL(coverURL: String?): String {
        if (coverURL == null){
            return ""
        } else {
            val url = URI(coverURL)
            val segments = url.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            //TODO: MAKE STATIC FUNCTION BELOW
            return "https://images.igdb.com/igdb/image/upload/t_cover_big/${imageHash}.jpg"
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

}


