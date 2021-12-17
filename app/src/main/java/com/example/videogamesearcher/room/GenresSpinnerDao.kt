package com.example.videogamesearcher.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.videogamesearcher.models.explore_spinners.GenericSpinnerItem
import com.example.videogamesearcher.models.explore_spinners.GenresResponseItem

@Dao
interface GenresSpinnerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGenresList(spinnerList: GenresResponseItem)

    @Transaction
    @Query("SELECT * FROM genres_list_table ORDER BY name ASC")
    fun getGenresListData(): LiveData<List<GenericSpinnerItem>>

}