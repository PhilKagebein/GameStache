package com.stache.gamestache.room

import androidx.room.*
import com.stache.gamestache.models.TwitchAuthorization

@Dao
interface TwitchAuthorizationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTwitchAuthToDb(twitchAuthorization: TwitchAuthorization)

    @Transaction
    @Query("SELECT * FROM auth_table")
    fun getTwitchAuthFromDb(): TwitchAuthorization

    @Transaction
    @Query("SELECT COUNT(*) FROM auth_table")
    fun checkIfAuthTokenIsInDB(): Int

}