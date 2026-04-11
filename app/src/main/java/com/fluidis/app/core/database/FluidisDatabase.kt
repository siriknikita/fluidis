package com.fluidis.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fluidis.app.core.model.DrinkEntry

@Database(
    entities = [DrinkEntry::class],
    version = 1,
    exportSchema = true,
)
abstract class FluidisDatabase : RoomDatabase() {
    abstract fun drinkEntryDao(): DrinkEntryDao
}
