package com.example.gamestache.koin

import android.app.Application
import com.example.gamestache.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class GameStacheApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@GameStacheApplication)
            modules(
                apiAuthModule,
                twitchApiModule,
                gameStacheDatabaseModule,
                gameStacheRepositoryModule,
                exploreViewModelModule,
                individualGameViewModelModule
            )
        }
    }
}