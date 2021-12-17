package com.example.videogamesearcher.ui.individual_game

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.videogamesearcher.models.individual_game.IndividualGameDataItem

@Dao
interface IndividualGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeIndividualGameDataInRoom(individualGameData: List<IndividualGameDataItem>)

    @Transaction
    @Query("SELECT * FROM individual_game_table WHERE id = :gameID")
    fun getIndividualGameDataFromRoom(gameID: Int): LiveData<List<IndividualGameDataItem?>>

    @Transaction
    @Query("SELECT COUNT(id) FROM individual_game_table WHERE id = :gameID")
    fun checkIfGameExistsInRoom(gameID: Int): LiveData<Int>
}