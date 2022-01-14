package com.example.gamestache.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gamestache.models.explore_spinners.GenericSpinnerItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem

@Dao
interface GenresSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGenresListToDb(spinnerList: GenresResponseItem)

    @Transaction
    @Query("SELECT * FROM genres_list_table ORDER BY name ASC")
    fun getGenresListFromDb(): LiveData<MutableList<GenericSpinnerItem>>

}