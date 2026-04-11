package com.fluidis.app.feature.home

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.DrinkTypeTotal
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DrinkEntryDao
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        settingsDataStore = mockk()

        every { dao.getEntriesForDate(any()) } returns flowOf(emptyList())
        every { dao.getTotalsPerDrinkForDate(any()) } returns flowOf(emptyList())
        every { settingsDataStore.settings } returns flowOf(Settings())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(dao, settingsDataStore)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Success with empty entries`() = runTest {
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as HomeUiState.Success
            assertEquals(0, success.totalMl)
            assertEquals(2000, success.goalMl)
            assertEquals(0f, success.progressFraction)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Success with entries`() = runTest {
        val entries = listOf(
            DrinkEntry(1, "water", 500, "2026-04-11", 1000L),
            DrinkEntry(2, "tea", 350, "2026-04-11", 2000L),
        )
        every { dao.getEntriesForDate(any()) } returns flowOf(entries)
        every { dao.getTotalsPerDrinkForDate(any()) } returns flowOf(
            listOf(
                DrinkTypeTotal("water", 500),
                DrinkTypeTotal("tea", 350),
            )
        )

        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as HomeUiState.Success
            assertEquals(850, success.totalMl)
            assertEquals(500, success.drinkTotals[DrinkType.WATER])
            assertEquals(350, success.drinkTotals[DrinkType.TEA])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addEntry inserts into dao`() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        viewModel = createViewModel()

        viewModel.addEntry(DrinkType.WATER, 500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { dao.insert(match { it.drinkType == "water" && it.amountMl == 500 }) }
    }

    @Test
    fun `addEntry rejects zero amount`() = runTest {
        viewModel = createViewModel()

        viewModel.addEntry(DrinkType.WATER, 0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `undoLastEntry deletes and emits event`() = runTest {
        val entry = DrinkEntry(1, "water", 500, "2026-04-11", 1000L)
        coEvery { dao.getLastEntryForDrinkOnDate(any(), "water") } returns entry
        coEvery { dao.delete(entry) } returns Unit

        viewModel = createViewModel()

        viewModel.events.test {
            viewModel.undoLastEntry(DrinkType.WATER)
            testDispatcher.scheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is HomeEvent.EntryRemoved)
            assertEquals(entry, (event as HomeEvent.EntryRemoved).entry)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `goal reached computed correctly`() {
        val state = HomeUiState.Success(
            totalMl = 2500,
            goalMl = 2000,
            drinkTotals = emptyMap(),
            drinkServingCounts = emptyMap(),
            maxServings = emptyMap(),
            recentEntries = emptyList(),
            servingSizes = emptyMap(),
        )
        assertTrue(state.goalReached)
        assertEquals(500, state.overGoalMl)
        assertEquals(0, state.remainingMl)
    }
}
