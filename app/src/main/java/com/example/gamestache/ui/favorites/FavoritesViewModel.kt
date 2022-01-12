package com.example.gamestache.ui.favorites

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.*
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
                val favoritesMassaged = massageDataForListAdapter(listOfFavorites)
                favoritesListFromDb.postValue(favoritesMassaged)
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
            return concatCoverUrl(imageHash)
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


