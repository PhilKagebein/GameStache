package com.example.videogamesearcher.models.search_results

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GameMode(
    @PrimaryKey

    val id: Int,
    var name: String
)