package com.example.gamestache.models.explore_spinners

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platforms_list_table")
data class PlatformsResponseItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String
)


