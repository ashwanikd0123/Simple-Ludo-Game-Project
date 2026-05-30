package com.example.simpleludogame.di

import android.content.Context
import com.example.simpleludogame.game.MediaManager
import com.example.simpleludogame.game.gamemodel.GameConstants
import com.example.simpleludogame.game.gamemodel.dicemodel.Dice
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMediaManager(@ApplicationContext context: Context): MediaManager {
        return MediaManager(context)
    }

    @Provides
    fun provideDice(@ApplicationContext context: Context): Dice {
        return Dice(context)
    }

    @Provides
    fun provideGameConstants(@ApplicationContext context: Context): GameConstants {
        return GameConstants(context)
    }
}
