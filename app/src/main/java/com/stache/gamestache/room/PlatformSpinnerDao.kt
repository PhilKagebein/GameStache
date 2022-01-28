package com.stache.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.stache.gamestache.models.explore_spinners.GenericSpinnerItem
import com.stache.gamestache.models.explore_spinners.PlatformsResponseItem

@Dao
interface PlatformSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPlatformsListToDb(spinnerList: PlatformsResponseItem)

    @Transaction
    @Query("SELECT * FROM platforms_list_table ORDER BY name ASC")
    fun getPlatformsListFromDb(): LiveData<MutableList<GenericSpinnerItem>>

}