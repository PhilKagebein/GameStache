package com.stache.gamestache.koin

import android.content.Context
import com.stache.gamestache.Constants
import com.stache.gamestache.api.TwitchApi
import com.stache.gamestache.api.TwitchApiAuth
import com.stache.gamestache.repository.GameStacheRepository
import com.stache.gamestache.room.*
import com.stache.gamestache.ui.explore.ExploreViewModel
import com.stache.gamestache.ui.favorites.FavoritesViewModel
import com.stache.gamestache.ui.individual_game.IndividualGameViewModel
import com.stache.gamestache.ui.wishlist.WishlistViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiAuthModule = module {

    fun provideTwitchApiAuthToken(retrofitAuthToken: Retrofit): TwitchApiAuth {
        return retrofitAuthToken.create(TwitchApiAuth::class.java)
    }

    fun provideRetrofitAuthToken(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.AUTH_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { provideTwitchApiAuthToken(provideRetrofitAuthToken()) }

}

val twitchApiModule = module {

    fun provideTwitchApi(retrofit: Retrofit): TwitchApi {
        return retrofit.create(TwitchApi::class.java)
    }

    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { provideTwitchApi(provideRetrofit())}

}

val gameStacheDatabaseModule = module {


    fun provideGameStacheDatabase(context: Context): GameStacheDatabase {
        return GameStacheDatabase.getGameStacheDatabase(context)
    }

    fun providePlatformSpinnerDao(database: GameStacheDatabase): PlatformSpinnerDao {
        return database.platformSpinnerDao()
    }

    fun provideGenreSpinnerDao(database: GameStacheDatabase): GenresSpinnerDao {
        return database.genresSpinnerDao()
    }

    fun provideGameModeSpinnerDao(database: GameStacheDatabase): GameModesSpinnerDao {
        return database.gameModesSpinnerDao()
    }

    fun provideIndividualGameDataDao(database: GameStacheDatabase): IndividualGameDao {
        return database.individualGameDao()
    }

    fun provideTwitchAuthorizationDao(database: GameStacheDatabase): TwitchAuthorizationDao {
        return database.twitchAuthDao()
    }

    fun provideFavoritesDao(database: GameStacheDatabase): FavoritesAndWishlistDao {
        return database.favoritesDao()
    }

    single { provideGameStacheDatabase(androidApplication()) }
    single { providePlatformSpinnerDao(get()) }
    single { provideGenreSpinnerDao(get()) }
    single { provideGameModeSpinnerDao(get()) }
    single { provideIndividualGameDataDao(get()) }
    single { provideTwitchAuthorizationDao(get()) }
    single { provideFavoritesDao(get()) }

}

val gameStacheRepositoryModule = module {

    fun provideGameStacheRepository(twitchApi: TwitchApi, authApi: TwitchApiAuth, individualGameDao: IndividualGameDao, platformsSpinnerDao: PlatformSpinnerDao, genresSpinnerDao: GenresSpinnerDao, gameModesSpinnerDao: GameModesSpinnerDao, twitchAuthorizationDao: TwitchAuthorizationDao, favoritesAndWishlistDao: FavoritesAndWishlistDao): GameStacheRepository {
        return GameStacheRepository(twitchApi, authApi, individualGameDao, platformsSpinnerDao, genresSpinnerDao, gameModesSpinnerDao, twitchAuthorizationDao, favoritesAndWishlistDao)
    }

    single { provideGameStacheRepository(get(), get(), get(), get(), get(), get(), get(), get()) }
}


val exploreViewModelModule = module {

    viewModel { ExploreViewModel(get(), get()) }

}

val individualGameViewModelModule = module {

    viewModel { IndividualGameViewModel(get(), get())}

    single { androidApplication().resources }

}

val favoritesViewModel = module {

    viewModel { FavoritesViewModel(get()) }
}

val wishlistViewModel = module {

    viewModel { WishlistViewModel(get()) }
}
