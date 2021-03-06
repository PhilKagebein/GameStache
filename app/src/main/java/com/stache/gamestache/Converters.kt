package com.stache.gamestache

import androidx.room.TypeConverter
import com.stache.gamestache.models.Cover
import com.stache.gamestache.models.GameMode
import com.stache.gamestache.models.Genre
import com.stache.gamestache.models.Platform
import com.stache.gamestache.models.individual_game.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {

    @TypeConverter
    fun stringToCover(data: String?): Cover? {
        val coverType = object :
            TypeToken<Cover?>() {}.type
        return Gson().fromJson<Cover?>(data, coverType)
    }

    @TypeConverter
    fun coverToString(coverObject: Cover?): String?{
        return Gson().toJson(coverObject)
    }

    @TypeConverter
    fun stringToListGenre(data: String?): List<Genre?>? {
        val listGenreType = object :
            TypeToken<List<Genre?>?>() {}.type
        return Gson().fromJson<List<Genre?>?>(data, listGenreType)
    }

    @TypeConverter
    fun listGenreToString(genreObject: List<Genre?>?): String?{
        return Gson().toJson(genreObject)
    }

    @TypeConverter
    fun stringToGameModeList(data: String?): List<GameMode?>? {
        val gameModeListType = object :
            TypeToken<List<GameMode?>?>() {}.type
        return Gson().fromJson<List<GameMode?>?>(data, gameModeListType)
    }

    @TypeConverter
    fun gameModeListToString(individualGameModeObject: List<GameMode?>?): String?{
        return Gson().toJson(individualGameModeObject)
    }

    @TypeConverter
    fun stringToInvolvedCompanyList(data: String?): List<InvolvedCompany?>? {
        val involvedCompanyList = object :
            TypeToken<List<InvolvedCompany?>?>() {}.type
        return Gson().fromJson<List<InvolvedCompany?>?>(data, involvedCompanyList)
    }

    @TypeConverter
    fun involvedCompanyListToString(involvedCompanyObject: List<InvolvedCompany?>?): String?{
        return Gson().toJson(involvedCompanyObject)
    }

    @TypeConverter
    fun stringToCompany(data: String?): Company? {
        val companyType = object :
            TypeToken<Company?>() {}.type
        return Gson().fromJson<Company?>(data, companyType)
    }

    @TypeConverter
    fun companyToString(companyObject: Company?): String?{
        return Gson().toJson(companyObject)
    }

    @TypeConverter
    fun stringToPlatformList(data: String?): List<Platform?>? {
        val platformListType = object :
            TypeToken<List<Platform?>?>() {}.type
        return Gson().fromJson<List<Platform?>?>(data, platformListType)
    }

    @TypeConverter
    fun platformListToString(platformObject: List<Platform?>?): String?{
        return Gson().toJson(platformObject)
    }

    @TypeConverter
    fun stringToPlayerPerspectiveList(data: String?): List<PlayerPerspective?>? {
        val playerPerspectiveList = object :
            TypeToken<List<PlayerPerspective?>?>() {}.type
        return Gson().fromJson<List<PlayerPerspective?>?>(data, playerPerspectiveList)
    }

    @TypeConverter
    fun playerPerspectiveListToString(playerPerspectiveObject: List<PlayerPerspective?>?): String?{
        return Gson().toJson(playerPerspectiveObject)
    }

    @TypeConverter
    fun stringToReleaseDateList(data: String?): List<ReleaseDate?>? {
        val releaseDateList = object :
            TypeToken<List<ReleaseDate?>?>() {}.type
        return Gson().fromJson<List<ReleaseDate?>?>(data, releaseDateList)
    }

    @TypeConverter
    fun releaseDateListToString(releaseDateObject: List<ReleaseDate?>?): String?{
        return Gson().toJson(releaseDateObject)
    }

    @TypeConverter
    fun stringToSimilarGamesList(data: String?): List<SimilarGame?>? {
        val similarGamesType = object :
            TypeToken<List<SimilarGame?>?>() {}.type
        return Gson().fromJson<List<SimilarGame?>?>(data, similarGamesType)
    }

    @TypeConverter
    fun similarGameListToString(similarGameObject: List<SimilarGame?>?): String?{
        return Gson().toJson(similarGameObject)
    }

    @TypeConverter
    fun stringToMultiPlayerModesList(data: String?): List<MultiplayerModesItem?>? {
        val multiplayerModesItemType = object :
            TypeToken<List<MultiplayerModesItem?>?>() {}.type
        return Gson().fromJson<List<MultiplayerModesItem?>?>(data, multiplayerModesItemType)
    }

    @TypeConverter
    fun multiplayerModesListToString(multiplayerModesObject: List<MultiplayerModesItem?>?): String?{
        return Gson().toJson(multiplayerModesObject)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

}