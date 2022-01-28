package com.stache.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stache.gamestache.models.explore_spinners.GameModesResponseItem
import com.stache.gamestache.models.explore_spinners.GenericSpinnerItem

@Dao
interface GameModesSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGameModesListToDb(spinner: GameModesResponseItem)

    @Transaction
    @Query("SELECT * FROM game_modes_list_table ORDER BY name ASC")
    fun getGameModesListFromDb(): LiveData<MutableList<GenericSpinnerItem>>
}