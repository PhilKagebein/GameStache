package com.example.videogamesearcher.models.individual_game

import androidx.room.Entity

@Entity
data class IndividualGameMode(
    val id: Int,
    val name: String
)