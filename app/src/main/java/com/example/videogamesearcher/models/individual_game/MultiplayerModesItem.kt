package com.example.videogamesearcher.models.individual_game

data class MultiplayerModesItem(
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