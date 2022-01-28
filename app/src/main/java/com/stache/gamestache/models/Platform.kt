package com.stache.gamestache.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Platform(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var name: String
)