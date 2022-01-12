package com.example.gamestache.ui.wishlist

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI

class WishlistViewModel(private val gameStacheRepository: GameStacheRepository) : ViewModel() {

    val wishlistFromDb: MutableLiveData<List<SearchResultsResponseItem?>> = MutableLiveData()

    fun pullWishlistFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepository.getWishlistFromDb(true).let { wishlist ->
                val wishlistMassaged = massageGameCoverUrl(wishlist)
                wishlistFromDb.postValue(wishlistMassaged)
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
                game.cover.url = GameStacheRepository.concatCoverUrl(imageHash)
            }

        }

        return gamesList

    }

    fun setNoWishlistTextViewVisibility(wishlist: List<SearchResultsResponseItem?>): Int {
        if (wishlist.isNullOrEmpty()) return View.VISIBLE
        else return View.GONE
    }

    fun setWishlistRecyclerViewVisibility(wishlist: List<SearchResultsResponseItem?>): Int {
        if (wishlist.isNullOrEmpty()) return View.GONE
        else return View.VISIBLE
    }

    fun filterWishlist(filterQuery: String): LiveData<List<SearchResultsResponseItem>> {
        return gameStacheRepository.filterWishlist(filterQuery, true)
    }
}