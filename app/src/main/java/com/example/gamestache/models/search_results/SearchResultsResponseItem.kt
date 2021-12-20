package com.example.gamestache.models.search_results

data class SearchResultsResponseItem(
    val cover: Cover?,
    var game_modes: List<GameMode?>,
    var genres: List<Genre?>,
    val id: Int,
    val name: String,
    var platforms: List<Platform?>
)