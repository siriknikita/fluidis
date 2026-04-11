package com.fluidis.app.feature.statistics

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkTypeTotal
import com.fluidis.app.core.model.Settings
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

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
            listOf(
                DailyTotal("2026-04-10", 1500),
                DailyTotal("2026-04-11", 2200),
            )
        )
        every { dao.getTotalsPerDrinkInRange(any(), any()) } returns flowOf(
            listOf(
                DrinkTypeTotal("water", 2000),
                DrinkTypeTotal("tea", 700),
                DrinkTypeTotal("coffee", 1000),
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `computes averages correctly`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as StatisticsUiState.Success
            assertEquals(1850, success.averageMl) // (1500 + 2200) / 2
            assertEquals(2, success.daysTracked)
            assertEquals(1, success.daysGoalMet) // only 2200 >= 2000
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `default period is WEEK`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as StatisticsUiState.Success
            assertEquals(StatsPeriod.WEEK, success.period)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setChartMode persists to DataStore`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore)
        io.mockk.coEvery { settingsDataStore.updateSelectedChartMode(any()) } returns Unit

        viewModel.setChartMode(ChartMode.LINE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsDataStore.updateSelectedChartMode(ChartMode.LINE) }
    }
}
