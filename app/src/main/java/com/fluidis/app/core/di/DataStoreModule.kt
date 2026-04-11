package com.fluidis.app.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule
// SettingsDataStore is @Singleton @Inject constructor — no manual binding needed.
// This module exists as a placeholder for future DataStore-related bindings.
