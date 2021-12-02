package com.example.videogamesearcher.models.explore_spinners

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "platforms_list_table")
data class PlatformsResponseItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String
)


