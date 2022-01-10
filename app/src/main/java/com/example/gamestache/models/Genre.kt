package com.example.gamestache.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Genre(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var name: String
)