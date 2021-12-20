package com.example.gamestache.models.search_results

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameMode(
    @PrimaryKey
    val id: Int = 0,
    var name: String = ""
)