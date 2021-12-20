package com.example.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem

@Dao
interface GameModesSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameModesList(spinner: GameModesResponseItem)

    @Transaction
    @Query("SELECT * FROM game_modes_list_table ORDER BY name ASC")
    fun getGameModesListData(): LiveData<List<GenericSpinnerItem>>
}