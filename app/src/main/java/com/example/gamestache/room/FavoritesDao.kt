package com.example.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gamestache.models.search_results.SearchResultsResponseItem

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameAsFavorite(favoriteGame: SearchResultsResponseItem?)

    @Delete
    suspend fun removeGameAsFavorite(favoriteGame: SearchResultsResponseItem?)

    @Transaction
    @Query("SELECT * FROM fave_table")
    fun getFavoritesFromDb(): List<SearchResultsResponseItem?>

    @Transaction
    @Query("SELECT COUNT(id) FROM fave_table WHERE id = :gameId")
    suspend fun checkIfGameIsFavorited(gameId: Int): Int

    @Transaction
    @Query("SELECT * FROM fave_table WHERE name LIKE :filterQuery")
    fun filterFavoriteGames(filterQuery: String): LiveData<List<SearchResultsResponseItem>>

}