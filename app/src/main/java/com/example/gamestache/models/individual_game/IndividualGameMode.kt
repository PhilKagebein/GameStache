package com.example.gamestache.models.individual_game

import androidx.room.Entity

@Entity
data class IndividualGameMode(
    val id: Int,
    val name: String
)