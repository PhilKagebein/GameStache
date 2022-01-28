package com.stache.gamestache.models.individual_game

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
    val offlinecoopmax: Int?,
    val offlinemax: Int?,
    val onlinecoop: Boolean,
    val onlinecoopmax: Int?,
    val onlinemax: Int?,
    val platform: Int,
    val splitscreen: Boolean
)