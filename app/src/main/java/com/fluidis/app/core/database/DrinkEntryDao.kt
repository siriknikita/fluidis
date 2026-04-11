package com.fluidis.app.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkTypeTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkEntryDao {

    @Insert
    suspend fun insert(entry: DrinkEntry): Long

    @Update
    suspend fun update(entry: DrinkEntry)

    @Delete
    suspend fun delete(entry: DrinkEntry)

    @Query("SELECT * FROM drink_entries WHERE date = :date ORDER BY createdAt DESC")
    fun getEntriesForDate(date: String): Flow<List<DrinkEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drink_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int>

    @Query(
        """
        SELECT drinkType, COALESCE(SUM(amountMl), 0) AS total
        FROM drink_entries
        WHERE date = :date
        GROUP BY drinkType
        """
    )
    fun getTotalsPerDrinkForDate(date: String): Flow<List<DrinkTypeTotal>>

    @Query(
        """
        SELECT date, COALESCE(SUM(amountMl), 0) AS total
        FROM drink_entries
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date
        """
    )
    fun getDailyTotalsInRange(startDate: String, endDate: String): Flow<List<DailyTotal>>

    @Query(
        """
        SELECT drinkType, COALESCE(SUM(amountMl), 0) AS total
        FROM drink_entries
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY drinkType
        """
    )
    fun getTotalsPerDrinkInRange(startDate: String, endDate: String): Flow<List<DrinkTypeTotal>>

    @Query(
        """
        SELECT DISTINCT date
        FROM drink_entries
        WHERE date BETWEEN :startDate AND :endDate
        """
    )
    fun getDatesWithEntries(startDate: String, endDate: String): Flow<List<String>>

    @Query("SELECT MIN(date) FROM drink_entries")
    fun getEarliestEntryDate(): Flow<String?>

    @Query(
        """
        SELECT * FROM drink_entries
        WHERE date = :date AND drinkType = :drinkType
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getLastEntryForDrinkOnDate(date: String, drinkType: String): DrinkEntry?

    @Query("DELETE FROM drink_entries WHERE date = :date AND drinkType = :drinkType")
    suspend fun deleteAllForDrinkOnDate(date: String, drinkType: String)
}
