package com.example.gamestache.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "individual_game_table")
data class IndividualGameDataItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int?,
    val cover: Cover?,
    val first_release_date: Int?,
    val genres: List<Genre?>?,
    val game_modes: List<IndividualGameMode?>?,
    val involved_companies: List<InvolvedCompany?>?,
    val name: String?,
    val platforms: List<Platform?>?,
    val player_perspectives: List<PlayerPerspective?>?,
    val release_dates: List<ReleaseDate?>?,
    val similar_games: List<SimilarGame?>?,
    val summary: String?,
    val multiplayer_modes: List<MultiplayerModesItem?>?
)
