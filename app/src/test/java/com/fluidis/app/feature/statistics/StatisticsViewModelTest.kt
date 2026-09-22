package com.fluidis.app.feature.statistics

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DailyDrinkTotal
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
import org.junit.Assert.assertNull
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
        every { dao.getDailyDrinkTotalsInRange(any(), any()) } returns flowOf(
            listOf(
                DailyDrinkTotal("2026-09-10", "water", 2000), // the previous week
                DailyDrinkTotal("2026-09-11", "water", 1000),
                DailyDrinkTotal("2026-09-20", "water", 1000),
                DailyDrinkTotal("2026-09-20", "tea", 500),
                DailyDrinkTotal("2026-09-21", "water", 2200),
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `compares the week with the week before`() = runTest {
        val viewModel = StatisticsViewModel(dao, settingsDataStore, todayProvider)

        viewModel.uiState.test {
            awaitItem() // Loading
            val trend = (awaitItem() as StatisticsUiState.Success).report.trend
            assertEquals(1850, trend.current.averageMl) // (1500 + 2200) / 2
            assertEquals(2, trend.current.daysTracked)
            assertEquals(1, trend.current.metDays) // only 2200 >= 2000
            assertEquals(1500, trend.previous?.averageMl)
            assertEquals(1, trend.previous?.metDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `week range is seven days ending today, compared with the seven before`() {
        val today = LocalDate(2026, 9, 22)
        val week = StatisticsViewModel.daysFor(StatsPeriod.WEEK, null, today)
        assertEquals(LocalDate(2026, 9, 16)..today, week)
        assertEquals(
            LocalDate(2026, 9, 9)..LocalDate(2026, 9, 15),
            StatisticsViewModel.previousDays(StatsPeriod.WEEK, week),
        )
        assertNull(StatisticsViewModel.previousDays(StatsPeriod.ALL_TIME, week))
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

        coVerify { dao.getDailyDrinkTotalsInRange(any(), "2026-09-21") }
        coVerify { dao.getDailyDrinkTotalsInRange(any(), "2026-09-22") }
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
