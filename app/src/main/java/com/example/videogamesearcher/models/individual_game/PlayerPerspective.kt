package com.example.videogamesearcher.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
data class PlayerPerspective(
    @PrimaryKey
    val id: Int?,
    val name: String?
)