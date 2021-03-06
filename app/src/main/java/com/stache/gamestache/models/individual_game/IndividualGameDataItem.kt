package com.stache.gamestache.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stache.gamestache.models.Cover
import com.stache.gamestache.models.GameMode
import com.stache.gamestache.models.Genre
import com.stache.gamestache.models.Platform

@Entity(tableName = "individual_game_table")
data class IndividualGameDataItem(
    @PrimaryKey(autoGenerate = false)
    val id: Int?,
    var cover: Cover?,
    val first_release_date: Int?,
    val genres: List<Genre?>?,
    val game_modes: List<GameMode?>?,
    val involved_companies: List<InvolvedCompany?>?,
    val name: String?,
    val platforms: List<Platform?>?,
    val player_perspectives: List<PlayerPerspective?>?,
    val release_dates: List<ReleaseDate?>?,
    val similar_games: List<SimilarGame?>?,
    val summary: String?,
    val multiplayer_modes: List<MultiplayerModesItem?>?,
)
