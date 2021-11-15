package com.example.videogamesearcher.models.explore_spinners

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "game_modes_list_table")
data class GameModesResponseItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String

)
