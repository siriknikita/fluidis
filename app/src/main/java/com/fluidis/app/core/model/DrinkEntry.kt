package com.fluidis.app.core.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drink_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "drinkType"]),
    ],
)
data class DrinkEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val drinkType: String,
    val amountMl: Int,
    val date: String,
    val createdAt: Long,
)
