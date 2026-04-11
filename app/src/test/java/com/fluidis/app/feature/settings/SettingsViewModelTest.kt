package com.fluidis.app.feature.settings

import app.cash.turbine.test
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: DrinkEntryDao
    private lateinit var settingsDataStore: SettingsDataStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dao = mockk(relaxed = true)
        settingsDataStore = mockk()

        every { settingsDataStore.settings } returns flowOf(Settings())
        every { dao.getEarliestEntryDate() } returns flowOf("2026-04-01")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads settings with defaults`() = runTest {
        val viewModel = SettingsViewModel(settingsDataStore, dao)

        viewModel.uiState.test {
            awaitItem() // Loading
            val success = awaitItem() as SettingsUiState.Success
            assertEquals(2000, success.goalMl)
            assertEquals(500, success.servingSizes[DrinkType.WATER])
            assertEquals(350, success.servingSizes[DrinkType.TEA])
            assertEquals(350, success.servingSizes[DrinkType.COFFEE])
            assertNull(success.analyticsStartDate)
            assertEquals(LocalDate(2026, 4, 1), success.detectedStartDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDailyGoal calls DataStore`() = runTest {
        coEvery { settingsDataStore.updateDailyGoal(any()) } returns Unit
        val viewModel = SettingsViewModel(settingsDataStore, dao)

        viewModel.updateDailyGoal(2500)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsDataStore.updateDailyGoal(2500) }
    }

    @Test
    fun `updateDailyGoal rejects zero`() = runTest {
        val viewModel = SettingsViewModel(settingsDataStore, dao)

        viewModel.updateDailyGoal(0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { settingsDataStore.updateDailyGoal(any()) }
    }

    @Test
    fun `updateServingSize calls DataStore`() = runTest {
        coEvery { settingsDataStore.updateServingSize(any(), any()) } returns Unit
        val viewModel = SettingsViewModel(settingsDataStore, dao)

        viewModel.updateServingSize(DrinkType.WATER, 250)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsDataStore.updateServingSize(DrinkType.WATER, 250) }
    }

    @Test
    fun `updateAnalyticsStartDate calls DataStore`() = runTest {
        coEvery { settingsDataStore.updateAnalyticsStartDate(any()) } returns Unit
        val viewModel = SettingsViewModel(settingsDataStore, dao)

        val date = LocalDate(2026, 3, 1)
        viewModel.updateAnalyticsStartDate(date)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsDataStore.updateAnalyticsStartDate(date) }
    }
}
