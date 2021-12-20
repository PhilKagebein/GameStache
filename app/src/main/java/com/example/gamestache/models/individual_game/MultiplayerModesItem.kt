package com.example.gamestache.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MultiplayerModesItem(
    @PrimaryKey
    val campaigncoop: Boolean,
    val checksum: String,
    val dropin: Boolean,
    val game: Int,
    val id: Int,
    val lancoop: Boolean,
    val offlinecoop: Boolean,
    val onlinecoop: Boolean,
    val platform: Int,
    val splitscreen: Boolean
)