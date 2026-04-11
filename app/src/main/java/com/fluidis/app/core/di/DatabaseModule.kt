package com.fluidis.app.core.di

import android.content.Context
import androidx.room.Room
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.database.FluidisDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FluidisDatabase =
        Room.databaseBuilder(
            context,
            FluidisDatabase::class.java,
            "fluidis.db",
        ).build()

    @Provides
    fun provideDrinkEntryDao(database: FluidisDatabase): DrinkEntryDao =
        database.drinkEntryDao()
}
