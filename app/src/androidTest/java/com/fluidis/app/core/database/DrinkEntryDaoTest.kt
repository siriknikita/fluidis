package com.fluidis.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fluidis.app.core.model.DrinkEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrinkEntryDaoTest {

    private lateinit var database: FluidisDatabase
    private lateinit var dao: DrinkEntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FluidisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.drinkEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entry(
        drinkType: String = "water",
        amountMl: Int = 500,
        date: String = "2026-04-11",
        createdAt: Long = System.currentTimeMillis(),
    ) = DrinkEntry(
        drinkType = drinkType,
        amountMl = amountMl,
        date = date,
        createdAt = createdAt,
    )

    @Test
    fun insertAndRetrieve() = runTest {
        dao.insert(entry())
        val entries = dao.getEntriesForDate("2026-04-11").first()
        assertEquals(1, entries.size)
        assertEquals("water", entries[0].drinkType)
        assertEquals(500, entries[0].amountMl)
    }

    @Test
    fun totalForDate() = runTest {
        dao.insert(entry(amountMl = 500))
        dao.insert(entry(amountMl = 350, drinkType = "tea"))
        val total = dao.getTotalForDate("2026-04-11").first()
        assertEquals(850, total)
    }

    @Test
    fun totalsPerDrinkForDate() = runTest {
        dao.insert(entry(amountMl = 500, drinkType = "water"))
        dao.insert(entry(amountMl = 250, drinkType = "water"))
        dao.insert(entry(amountMl = 350, drinkType = "tea"))

        val totals = dao.getTotalsPerDrinkForDate("2026-04-11").first()
        assertEquals(2, totals.size)

        val waterTotal = totals.find { it.drinkType == "water" }!!.total
        val teaTotal = totals.find { it.drinkType == "tea" }!!.total
        assertEquals(750, waterTotal)
        assertEquals(350, teaTotal)
    }

    @Test
    fun dailyTotalsInRange() = runTest {
        dao.insert(entry(date = "2026-04-10", amountMl = 1000))
        dao.insert(entry(date = "2026-04-11", amountMl = 500))
        dao.insert(entry(date = "2026-04-11", amountMl = 500))
        dao.insert(entry(date = "2026-04-12", amountMl = 2000))

        val totals = dao.getDailyTotalsInRange("2026-04-10", "2026-04-11").first()
        assertEquals(2, totals.size)
        assertEquals(1000, totals[0].total)
        assertEquals(1000, totals[1].total)
    }

    @Test
    fun deleteEntry() = runTest {
        val id = dao.insert(entry())
        val entries = dao.getEntriesForDate("2026-04-11").first()
        assertEquals(1, entries.size)

        dao.delete(entries[0])
        val afterDelete = dao.getEntriesForDate("2026-04-11").first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun updateEntry() = runTest {
        dao.insert(entry(amountMl = 500))
        val entries = dao.getEntriesForDate("2026-04-11").first()

        dao.update(entries[0].copy(amountMl = 250, drinkType = "tea"))
        val updated = dao.getEntriesForDate("2026-04-11").first()
        assertEquals(250, updated[0].amountMl)
        assertEquals("tea", updated[0].drinkType)
    }

    @Test
    fun getLastEntryForDrinkOnDate() = runTest {
        dao.insert(entry(createdAt = 1000L))
        dao.insert(entry(createdAt = 2000L))
        dao.insert(entry(drinkType = "tea", createdAt = 3000L))

        val last = dao.getLastEntryForDrinkOnDate("2026-04-11", "water")
        assertEquals(2000L, last!!.createdAt)
    }

    @Test
    fun getEarliestEntryDate() = runTest {
        dao.insert(entry(date = "2026-04-11"))
        dao.insert(entry(date = "2026-04-05"))
        dao.insert(entry(date = "2026-04-15"))

        val earliest = dao.getEarliestEntryDate().first()
        assertEquals("2026-04-05", earliest)
    }

    @Test
    fun getEarliestEntryDateReturnsNullWhenEmpty() = runTest {
        val earliest = dao.getEarliestEntryDate().first()
        assertNull(earliest)
    }

    @Test
    fun getDatesWithEntries() = runTest {
        dao.insert(entry(date = "2026-04-10"))
        dao.insert(entry(date = "2026-04-11"))
        dao.insert(entry(date = "2026-04-11"))

        val dates = dao.getDatesWithEntries("2026-04-01", "2026-04-30").first()
        assertEquals(2, dates.size)
    }
}
