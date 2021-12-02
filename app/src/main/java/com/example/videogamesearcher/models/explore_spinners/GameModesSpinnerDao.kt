package com.example.videogamesearcher.models.explore_spinners

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GameModesSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameModesList(spinner: GameModesResponseItem)

    @Transaction
    @Query("SELECT * FROM game_modes_list_table ORDER BY name ASC")
    fun getGameModesListData(): LiveData<List<GenericSpinnerItem>>
}