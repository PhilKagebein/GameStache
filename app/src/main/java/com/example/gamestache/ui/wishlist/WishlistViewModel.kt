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
                val wishlistMassaged = massageDataForListAdapter(wishlist)
                wishlistFromDb.postValue(wishlistMassaged)
            }
        }
    }

    private fun massageCoverUrl(coverURL: String?): String {
        if (coverURL == null){
            return ""
        } else {
            val url = URI(coverURL)
            val segments = url.path.split("/")
            val lastSegment = segments[segments.size - 1]
            val imageHash = lastSegment.substring(0, (lastSegment.length - 4))
            return GameStacheRepository.concatCoverUrl(imageHash)
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

    fun massageDataForListAdapter(gamesList: List<SearchResultsResponseItem?>): List<SearchResultsResponseItem?> {
        for (game in gamesList) {
            game?.cover?.url = game?.cover?.url?.let { massageCoverUrl(it) }.toString()

            val gameInfoListsMap = createStringMaps(game)

            game?.platformsToDisplay = joinInfoListsToString(gameInfoListsMap["platforms"])
            game?.genresToDisplay = joinInfoListsToString(gameInfoListsMap["genres"])
            game?.gameModesToDisplay = joinInfoListsToString(gameInfoListsMap["gameModes"])

        }
        return gamesList
    }

    private fun createStringMaps(game: SearchResultsResponseItem?): Map<String, MutableList<String>> {
        val platformList = mutableListOf<String>()
        val genreList = mutableListOf<String>()
        val gameModesList = mutableListOf<String>()

        game?.platforms?.let { platforms ->
            for (platform in platforms) {
                platform?.name?.let { platformList.add(it) }
            }
        }

        game?.genres?.let { genres ->
            for (genre in genres) {
                genre?.name?.let { genreList.add(it) }
            }
        }

        game?.game_modes?.let { gameModes ->
            for (gameMode in gameModes) {
                gameMode?.name?.let { gameModesList.add(it) }
            }
        }

        return mapOf("platforms" to platformList, "genres" to genreList, "gameModes" to gameModesList)
    }

    private fun joinInfoListsToString(list: MutableList<String>?): String {
        list?.let { return list.joinToString(separator = ", ") }
            ?: run {return ""}
    }
}