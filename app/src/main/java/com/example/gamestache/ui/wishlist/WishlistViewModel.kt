package com.example.gamestache.ui.wishlist

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestache.massageDataForListAdapter
import com.example.gamestache.models.search_results.SearchResultsResponseItem
import com.example.gamestache.repository.GameStacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WishlistViewModel(private val gameStacheRepository: GameStacheRepository) : ViewModel() {

    val wishlistFromDb: MutableLiveData<List<SearchResultsResponseItem?>> = MutableLiveData()

    fun pullWishlistFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            gameStacheRepository.getWishlistFromDb(true).let { wishlist ->
                val wishlistMassaged = massageDataForListAdapter(wishlist)
                wishlistFromDb.postValue(wishlistMassaged)
            }
        }
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