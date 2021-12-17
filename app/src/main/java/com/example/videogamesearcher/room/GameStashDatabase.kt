package com.example.videogamesearcher.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.videogamesearcher.Converters
import com.example.videogamesearcher.models.individual_game.IndividualGameDataItem
import com.example.videogamesearcher.models.explore_spinners.GameModesResponseItem
import com.example.videogamesearcher.models.explore_spinners.GenresResponseItem
import com.example.videogamesearcher.models.explore_spinners.PlatformsResponseItem

@Database(entities = [PlatformsResponseItem::class, GenresResponseItem::class, GameModesResponseItem::class, IndividualGameDataItem::class], version = 1, exportSchema = false)
@TypeConverters( Converters::class )
abstract class GameStashDatabase: RoomDatabase() {

    abstract fun platformSpinnerDao(): PlatformSpinnerDao
    abstract fun genresSpinnerDao(): GenresSpinnerDao
    abstract fun gameModesSpinnerDao(): GameModesSpinnerDao
    abstract fun individualGameDao(): IndividualGameDao

    companion object{
        @Volatile
        private var INSTANCE: GameStashDatabase? = null


        fun getGameStashDatabase(context: Context): GameStashDatabase {
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