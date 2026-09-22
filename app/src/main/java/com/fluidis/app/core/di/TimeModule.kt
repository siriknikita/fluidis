package com.fluidis.app.core.di

import com.fluidis.app.core.time.SystemTodayProvider
import com.fluidis.app.core.time.TodayProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    abstract fun bindTodayProvider(impl: SystemTodayProvider): TodayProvider
}
