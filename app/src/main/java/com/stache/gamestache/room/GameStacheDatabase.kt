package com.stache.gamestache.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stache.gamestache.Converters
import com.stache.gamestache.models.TwitchAuthorization
import com.stache.gamestache.models.individual_game.IndividualGameDataItem
import com.stache.gamestache.models.explore_spinners.GameModesResponseItem
import com.stache.gamestache.models.explore_spinners.GenresResponseItem
import com.stache.gamestache.models.explore_spinners.PlatformsResponseItem
import com.stache.gamestache.models.search_results.SearchResultsResponseItem

@Database(entities =
[PlatformsResponseItem::class,
    GenresResponseItem::class,
    GameModesResponseItem::class,
    IndividualGameDataItem::class,
    TwitchAuthorization::class,
    SearchResultsResponseItem::class
], version = 2, exportSchema = false)
@TypeConverters( Converters::class )
abstract class GameStacheDatabase: RoomDatabase() {

    abstract fun platformSpinnerDao(): PlatformSpinnerDao
    abstract fun genresSpinnerDao(): GenresSpinnerDao
    abstract fun gameModesSpinnerDao(): GameModesSpinnerDao
    abstract fun individualGameDao(): IndividualGameDao
    abstract fun twitchAuthDao(): TwitchAuthorizationDao
    abstract fun favoritesDao(): FavoritesAndWishlistDao

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
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}