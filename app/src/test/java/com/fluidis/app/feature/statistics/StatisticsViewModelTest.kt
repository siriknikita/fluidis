package com.fluidis.app.feature.statistics

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkTypeTotal
import com.fluidis.app.core.model.Settings
import com.fluidis.app.core.time.FakeTodayProvider
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DrinkEntryDao
    private lateinit var settingsDataStore: SettingsDataStore
    private val todayProvider = FakeTodayProvider(LocalDate(2026, 9, 21))

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
        val viewModel = StatisticsViewModel(dao, settingsDataStore, todayProvider)

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
        val viewModel = StatisticsViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as StatisticsUiState.Success
            assertEquals(StatsPeriod.WEEK, success.period)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `day rollover moves the range to end on the new day`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            skipItems(2) // Loading, first Success
            todayProvider.set(LocalDate(2026, 9, 22))
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { dao.getDailyTotalsInRange(any(), "2026-09-21") }
        coVerify { dao.getDailyTotalsInRange(any(), "2026-09-22") }
    }

    @Test
    fun `setChartMode persists to DataStore`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore, todayProvider)
        io.mockk.coEvery { settingsDataStore.updateSelectedChartMode(any()) } returns Unit

        viewModel.setChartMode(ChartMode.LINE)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsDataStore.updateSelectedChartMode(ChartMode.LINE) }
    }
}
