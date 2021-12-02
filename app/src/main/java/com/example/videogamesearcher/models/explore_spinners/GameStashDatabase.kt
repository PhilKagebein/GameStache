package com.example.videogamesearcher.models.explore_spinners

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlatformsResponseItem::class, GenresResponseItem::class, GameModesResponseItem::class], version = 1, exportSchema = false)
abstract class GameStashDatabase: RoomDatabase() {

    abstract fun platformSpinnerDao(): PlatformSpinnerDao
    abstract fun genresSpinnerDao(): GenresSpinnerDao
    abstract fun gameModesSpinnerDao(): GameModesSpinnerDao

    companion object{
        @Volatile
        private var INSTANCE: GameStashDatabase? = null


        fun getGameStashDatabase(context: Context): GameStashDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameStashDatabase::class.java,
                    "game_stash_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}