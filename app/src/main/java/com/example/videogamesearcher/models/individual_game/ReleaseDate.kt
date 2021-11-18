package com.example.videogamesearcher.models.individual_game

data class ReleaseDate(
    val date: Int,
    val game: Int,
    val human: String,
    val id: Int,
    val platform: PlatformReleaseDate,
    val region: Int
)