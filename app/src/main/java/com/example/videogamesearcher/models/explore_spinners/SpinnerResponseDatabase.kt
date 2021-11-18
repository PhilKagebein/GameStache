package com.example.videogamesearcher.models.explore_spinners

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlatformsResponseItem::class, GenresResponseItem::class, GameModesResponseItem::class], version = 1, exportSchema = false)
abstract class SpinnerResponseDatabase: RoomDatabase() {

    abstract  fun spinnerResponseDao(): SpinnerResponseDao

    companion object{
        @Volatile
        private var INSTANCE: SpinnerResponseDatabase? = null


        //@@@ktg I would much rather have one database with multiple tables than multiple databases
        // each with one table. That's how SQL-style databases are structured normally.
        // will also allow you to rename this class something like "<AppNameDatabase>" rather than
        // having the name be tied to any particular aspect of the data in the database or how it's used.
        fun getPlatformsListDatabase(context: Context): SpinnerResponseDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpinnerResponseDatabase::class.java,
                    "platforms_list_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

        fun getGenresListDatabase(context: Context): SpinnerResponseDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpinnerResponseDatabase::class.java,
                    "genres_list_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

        fun getGameModesListDatabase(context: Context): SpinnerResponseDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpinnerResponseDatabase::class.java,
                    "game_modes_list_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }


}