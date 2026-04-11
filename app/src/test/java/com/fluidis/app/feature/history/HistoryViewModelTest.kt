package com.fluidis.app.feature.history

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.Settings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DrinkEntryDao
    private lateinit var settingsDataStore: SettingsDataStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        settingsDataStore = mockk()

        every { settingsDataStore.settings } returns flowOf(Settings())
        every { dao.getDailyTotalsInRange(any(), any()) } returns flowOf(
            listOf(DailyTotal("2026-04-11", 1500))
        )
        every { dao.getEntriesForDate(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows current month`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as HistoryUiState.Success
            assertNull(success.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectDate toggles selection`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore)
        val date = LocalDate(2026, 4, 11)

        viewModel.selectDate(date)
        viewModel.uiState.test {
            skipItems(1) // Loading
            val success = awaitItem() as HistoryUiState.Success
            assertEquals(date, success.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteEntry calls dao`() = runTest {
        val entry = DrinkEntry(1, "water", 500, "2026-04-11", 1000L)
        coEvery { dao.delete(entry) } returns Unit

        val viewModel = HistoryViewModel(dao, settingsDataStore)
        viewModel.deleteEntry(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dao.delete(entry) }
    }

    @Test
    fun `updateEntry calls dao with modified entry`() = runTest {
        val entry = DrinkEntry(1, "water", 500, "2026-04-11", 1000L)
        coEvery { dao.update(any()) } returns Unit

        val viewModel = HistoryViewModel(dao, settingsDataStore)
        viewModel.updateEntry(entry, 250, DrinkType.TEA)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            dao.update(match { it.id == 1L && it.amountMl == 250 && it.drinkType == "tea" })
        }
    }

    @Test
    fun `addEntryToDate rejects zero amount`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore)
        viewModel.addEntryToDate(LocalDate(2026, 4, 11), DrinkType.WATER, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
