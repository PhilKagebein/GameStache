package com.example.videogamesearcher.models.explore_spinners

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SpinnerResponseDao {

    // @@@ktg you almost always want REPLACE rather than IGNORE. Ctrl+click into IGNORE here
    // and read about the differences between the strategies.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addPlatformsList(spinner: PlatformsResponseItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGenresList(spinner: GenresResponseItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGameModesList(spinner: GameModesResponseItem)

    @Transaction
    @Query("SELECT * FROM platforms_list_table ORDER BY name ASC")
    fun getPlatformsListData(): LiveData<List<PlatformsResponseItem>>

    @Transaction
    @Query("SELECT * FROM genres_list_table ORDER BY name ASC")
    fun getGenresListData(): LiveData<List<GenresResponseItem>>

    @Transaction
    @Query("SELECT * FROM game_modes_list_table ORDER BY name ASC")
    fun getGameModesListData(): LiveData<List<GameModesResponseItem>>
}