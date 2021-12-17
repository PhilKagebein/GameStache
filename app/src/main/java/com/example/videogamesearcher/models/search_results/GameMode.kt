package com.example.videogamesearcher.models.search_results

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
data class GameMode(
    @PrimaryKey
    val id: Int = 0,
    var name: String = ""
)