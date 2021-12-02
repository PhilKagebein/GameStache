package com.example.videogamesearcher.models.explore_spinners

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GenresSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGenresList(spinnerList: GenresResponseItem)

    @Transaction
    @Query("SELECT * FROM genres_list_table ORDER BY name ASC")
    fun getGenresListData(): LiveData<List<GenericSpinnerItem>>

}