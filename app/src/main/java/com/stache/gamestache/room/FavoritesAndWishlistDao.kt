package com.stache.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stache.gamestache.models.search_results.SearchResultsResponseItem

@Dao
interface FavoritesAndWishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameToFavoriteAndWishlistTable(game: SearchResultsResponseItem?)

    @Transaction
    @Query("UPDATE fave_and_wishlist_table SET favoriteStatus = :favoriteStatus, wishlistStatus = :wishlistStatus WHERE id = :gameId")
    suspend fun updateFavoriteAndWishlistStatus(gameId: Int, favoriteStatus: Boolean, wishlistStatus: Boolean)

    @Transaction
    @Query("SELECT * FROM fave_and_wishlist_table WHERE favoriteStatus = :favoriteStatus")
    fun getFavoritesFromDb(favoriteStatus: Boolean): List<SearchResultsResponseItem?>

    @Transaction
    @Query("SELECT * FROM fave_and_wishlist_table WHERE wishlistStatus = :wishlistStatus")
    fun getWishlistFromDb(wishlistStatus: Boolean): List<SearchResultsResponseItem?>

    @Transaction
    @Query("SELECT COUNT(id) FROM fave_and_wishlist_table WHERE id = :gameId")
    suspend fun checkIfGameIsInFavoriteAndWishlistTable(gameId: Int): Int

    @Transaction
    @Query("SELECT * FROM fave_and_wishlist_table WHERE name LIKE :filterQuery AND favoriteStatus = :favoriteStatus")
    fun filterFavoriteGames(filterQuery: String, favoriteStatus: Boolean): LiveData<List<SearchResultsResponseItem>>

    @Transaction
    @Query("SELECT * FROM fave_and_wishlist_table WHERE name LIKE :filterQuery AND wishlistStatus = :wishlistStatus")
    fun filterWishlist(filterQuery: String, wishlistStatus: Boolean): LiveData<List<SearchResultsResponseItem>>

    @Transaction
    @Query("SELECT * FROM fave_and_wishlist_table WHERE id = :gameId")
    fun getIndividualGameInfo(gameId: Int): SearchResultsResponseItem

}