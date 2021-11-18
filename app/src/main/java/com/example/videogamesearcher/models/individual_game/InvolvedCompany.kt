package com.example.videogamesearcher.models.individual_game

data class InvolvedCompany(
    val company: Company?,
    val developer: Boolean?,
    val id: Int?,
    val porting: Boolean?,
    val publisher: Boolean?,
    val supporting: Boolean?
)