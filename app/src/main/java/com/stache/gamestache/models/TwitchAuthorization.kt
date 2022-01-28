package com.stache.gamestache.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "auth_table")
data class TwitchAuthorization(
    @PrimaryKey(autoGenerate = false)
    val token_type: String,
    val access_token: String,
    val expires_in: Int,
    var token_birth_date: LocalDateTime
    )