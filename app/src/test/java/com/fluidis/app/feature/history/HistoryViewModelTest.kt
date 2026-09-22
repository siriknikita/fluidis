package com.fluidis.app.feature.history

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.Settings
import com.fluidis.app.core.time.FakeTodayProvider
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
import kotlinx.datetime.Month
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
    private val todayProvider = FakeTodayProvider(LocalDate(2026, 9, 30))

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
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as HistoryUiState.Success
            assertNull(success.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectDate toggles selection`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
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
    fun `month rollover moves the calendar to the new month`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.selectDate(LocalDate(2026, 9, 30))

        viewModel.uiState.test {
            skipItems(1) // Loading
            assertEquals(MonthYear(2026, Month.SEPTEMBER), (awaitItem() as HistoryUiState.Success).currentMonth)

            todayProvider.set(LocalDate(2026, 10, 1))
            val rolled = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(MonthYear(2026, Month.OCTOBER), rolled.currentMonth)
            assertEquals(LocalDate(2026, 10, 1), rolled.today)
            assertNull(rolled.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `month rollover leaves a browsed older month alone`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.navigateMonth(-2)

        viewModel.uiState.test {
            skipItems(1) // Loading
            awaitItem()

            todayProvider.set(LocalDate(2026, 10, 1))
            val state = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(MonthYear(2026, Month.JULY), state.currentMonth)
            assertEquals(LocalDate(2026, 10, 1), state.today)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<HistoryUiState>.expectMostRecentItemAfterIdle(): HistoryUiState {
        testDispatcher.scheduler.advanceUntilIdle()
        return expectMostRecentItem()
    }

    @Test
    fun `deleteEntry calls dao`() = runTest {
        val entry = DrinkEntry(1, "water", 500, "2026-04-11", 1000L)
        coEvery { dao.delete(entry) } returns Unit

        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.deleteEntry(entry)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dao.delete(entry) }
    }

    @Test
    fun `updateEntry calls dao with modified entry`() = runTest {
        val entry = DrinkEntry(1, "water", 500, "2026-04-11", 1000L)
        coEvery { dao.update(any()) } returns Unit

        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.updateEntry(entry, 250, DrinkType.TEA)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            dao.update(match { it.id == 1L && it.amountMl == 250 && it.drinkType == "tea" })
        }
    }

    @Test
    fun `addEntryToDate rejects zero amount`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.addEntryToDate(LocalDate(2026, 4, 11), DrinkType.WATER, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
