package com.example.videogamesearcher.models.individual_game

data class IndividualGameDataItem(
    val cover: Cover,
    val first_release_date: Int,
    val genres: List<Genre>,
    val id: Int,
    val involved_companies: List<InvolvedCompany>,
    val name: String,
    val platforms: List<Platform>,
    val player_perspectives: List<PlayerPerspective>,
    val release_dates: List<ReleaseDate>,
    val similar_games: List<SimilarGame>,
    val summary: String
)