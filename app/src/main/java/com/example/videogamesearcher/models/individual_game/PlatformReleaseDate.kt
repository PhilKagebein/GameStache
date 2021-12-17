package com.example.videogamesearcher.models.individual_game

import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Entity
data class PlatformReleaseDate(
    val id: Int,
    val name: String
)