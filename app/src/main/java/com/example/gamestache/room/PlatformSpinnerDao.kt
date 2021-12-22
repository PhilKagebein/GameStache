package com.example.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem

@Dao
interface PlatformSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlatformsListToDb(spinnerList: PlatformsResponseItem)

    @Transaction
    @Query("SELECT * FROM platforms_list_table ORDER BY name ASC")
    fun getPlatformsListFromDb(): LiveData<List<GenericSpinnerItem>>

}