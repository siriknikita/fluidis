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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun `today is selected initially`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as HistoryUiState.Success
            assertEquals(LocalDate(2026, 9, 30), success.selectedDate)
            assertFalse(success.isEntriesOpen)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting a date again keeps it selected`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        val date = LocalDate(2026, 9, 11)

        viewModel.selectDate(date)
        viewModel.selectDate(date)
        viewModel.uiState.test {
            skipItems(1) // Loading
            val success = awaitItem() as HistoryUiState.Success
            assertEquals(date, success.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigating months selects the day nearest today`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            skipItems(1) // Loading
            awaitItem()

            viewModel.navigateMonth(-1)
            val past = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(MonthYear(2026, Month.AUGUST), past.currentMonth)
            assertEquals(LocalDate(2026, 8, 31), past.selectedDate)

            viewModel.navigateMonth(2)
            val future = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(LocalDate(2026, 10, 1), future.selectedDate)

            viewModel.goToToday()
            val back = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(MonthYear(2026, Month.SEPTEMBER), back.currentMonth)
            assertEquals(LocalDate(2026, 9, 30), back.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the selected day is broken down per drink`() = runTest {
        every { dao.getEntriesForDate("2026-09-30") } returns flowOf(
            listOf(
                DrinkEntry(1, "water", 500, "2026-09-30", 3L),
                DrinkEntry(2, "water", 400, "2026-09-30", 2L),
                DrinkEntry(3, "coffee", 200, "2026-09-30", 1L),
            )
        )
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            skipItems(1) // Loading
            val day = (awaitItem() as HistoryUiState.Success).selectedDay
            assertEquals(1100, day.totalMl)
            assertEquals(DrinkType.entries, day.drinks.map { it.drinkType })
            assertEquals(listOf(900, 0, 200), day.drinks.map { it.totalMl })
            assertEquals(listOf(2, 0, 1), day.drinks.map { it.servings })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showing and dismissing entries keeps the day selected`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.uiState.test {
            skipItems(1) // Loading
            awaitItem()

            viewModel.showEntries()
            assertTrue((expectMostRecentItemAfterIdle() as HistoryUiState.Success).isEntriesOpen)

            viewModel.dismissDetail()
            val closed = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertFalse(closed.isEntriesOpen)
            assertEquals(LocalDate(2026, 9, 30), closed.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a day rollover moves a today selection along`() = runTest {
        val today = FakeTodayProvider(LocalDate(2026, 9, 21))
        val viewModel = HistoryViewModel(dao, settingsDataStore, today)

        viewModel.uiState.test {
            skipItems(1) // Loading
            awaitItem()
            today.set(LocalDate(2026, 9, 22))
            val state = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(LocalDate(2026, 9, 22), state.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a day rollover keeps a deliberately picked day`() = runTest {
        val today = FakeTodayProvider(LocalDate(2026, 9, 21))
        val viewModel = HistoryViewModel(dao, settingsDataStore, today)
        viewModel.selectDate(LocalDate(2026, 9, 10))

        viewModel.uiState.test {
            skipItems(1) // Loading
            awaitItem()
            today.set(LocalDate(2026, 9, 22))
            val state = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(LocalDate(2026, 9, 10), state.selectedDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `month rollover moves the calendar to the new month`() = runTest {
        val viewModel = HistoryViewModel(dao, settingsDataStore, todayProvider)
        viewModel.selectDate(LocalDate(2026, 9, 12))

        viewModel.uiState.test {
            skipItems(1) // Loading
            assertEquals(MonthYear(2026, Month.SEPTEMBER), (awaitItem() as HistoryUiState.Success).currentMonth)

            todayProvider.set(LocalDate(2026, 10, 1))
            val rolled = expectMostRecentItemAfterIdle() as HistoryUiState.Success
            assertEquals(MonthYear(2026, Month.OCTOBER), rolled.currentMonth)
            assertEquals(LocalDate(2026, 10, 1), rolled.today)
            assertEquals(LocalDate(2026, 10, 1), rolled.selectedDate)
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
            assertEquals(LocalDate(2026, 7, 31), state.selectedDate)
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
