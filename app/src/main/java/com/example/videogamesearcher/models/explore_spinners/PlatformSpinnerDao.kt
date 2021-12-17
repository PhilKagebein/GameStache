package com.example.videogamesearcher.models.explore_spinners

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlatformSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlatformsList(spinnerList: PlatformsResponseItem)

    @Transaction
    @Query("SELECT * FROM platforms_list_table ORDER BY name ASC")
    fun getPlatformsListData(): LiveData<List<GenericSpinnerItem>>

}