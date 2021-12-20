package com.example.gamestache.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gamestache.Converters
import com.example.gamestache.models.individual_game.IndividualGameDataItem
import com.example.gamestache.models.explore_spinners.GameModesResponseItem
import com.example.gamestache.models.explore_spinners.GenresResponseItem
import com.example.gamestache.models.explore_spinners.PlatformsResponseItem

@Database(entities = [PlatformsResponseItem::class, GenresResponseItem::class, GameModesResponseItem::class, IndividualGameDataItem::class], version = 1, exportSchema = false)
@TypeConverters( Converters::class )
abstract class GameStacheDatabase: RoomDatabase() {

    abstract fun platformSpinnerDao(): PlatformSpinnerDao
    abstract fun genresSpinnerDao(): GenresSpinnerDao
    abstract fun gameModesSpinnerDao(): GameModesSpinnerDao
    abstract fun individualGameDao(): IndividualGameDao

    companion object{
        @Volatile
        private var INSTANCE: GameStacheDatabase? = null


        fun getGameStacheDatabase(context: Context): GameStacheDatabase {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameStacheDatabase::class.java,
                    "game_stache_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}