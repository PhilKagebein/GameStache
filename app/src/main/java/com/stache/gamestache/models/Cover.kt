package com.stache.gamestache.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cover(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    var url: String
)