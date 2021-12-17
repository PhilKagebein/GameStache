package com.example.videogamesearcher.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cover(
    @PrimaryKey
    val id: Int,
    val url: String
)