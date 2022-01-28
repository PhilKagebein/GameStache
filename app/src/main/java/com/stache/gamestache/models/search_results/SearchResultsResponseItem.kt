package com.stache.gamestache.models.search_results

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stache.gamestache.models.Cover
import com.stache.gamestache.models.GameMode
import com.stache.gamestache.models.Genre
import com.stache.gamestache.models.Platform

@Entity(tableName = "fave_and_wishlist_table")
data class SearchResultsResponseItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int?,
    val cover: Cover?,
    var game_modes: List<GameMode?>?,
    var genres: List<Genre?>?,
    val name: String?,
    var platforms: List<Platform?>?,
    var favoriteStatus: Boolean,
    var wishlistStatus: Boolean,
    var platformsToDisplay: String? = "",
    var genresToDisplay: String? = "",
    var gameModesToDisplay: String? = ""
)