package com.stache.gamestache.models.individual_game

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class InvolvedCompany(
    @PrimaryKey
    val company: Company?,
    val developer: Boolean?,
    val id: Int?,
    val porting: Boolean?,
    val publisher: Boolean?,
    val supporting: Boolean?
)