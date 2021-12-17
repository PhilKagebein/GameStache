package com.example.videogamesearcher.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
data class ReleaseDate(
    @PrimaryKey
    val date: Int,
    val game: Int,
    val human: String,
    val id: Int,
    val platform: PlatformReleaseDate?,
    val region: Int
)