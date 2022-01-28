package com.stache.gamestache.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayerPerspective(
    @PrimaryKey
    val id: Int?,
    val name: String?
)