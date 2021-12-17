package com.example.videogamesearcher.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.videogamesearcher.models.explore_spinners.GenericSpinnerItem
import com.example.videogamesearcher.models.explore_spinners.PlatformsResponseItem

@Dao
interface PlatformSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlatformsList(spinnerList: PlatformsResponseItem)

    @Transaction
    @Query("SELECT * FROM platforms_list_table ORDER BY name ASC")
    fun getPlatformsListData(): LiveData<List<GenericSpinnerItem>>

}