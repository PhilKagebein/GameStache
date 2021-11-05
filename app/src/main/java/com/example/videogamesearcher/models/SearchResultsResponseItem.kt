package com.example.videogamesearcher.models

data class SearchResultsResponseItem(
    val cover: Cover,
    val game_modes: List<GameMode>,
    val genres: List<Genre>,
    val id: Int,
    val name: String,
    val platforms: List<Platform>
)