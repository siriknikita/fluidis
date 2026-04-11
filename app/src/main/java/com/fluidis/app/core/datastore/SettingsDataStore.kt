package com.fluidis.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val WATER_SERVING_ML = intPreferencesKey("water_serving_ml")
        val TEA_SERVING_ML = intPreferencesKey("tea_serving_ml")
        val COFFEE_SERVING_ML = intPreferencesKey("coffee_serving_ml")
        val WATER_MAX_SERVINGS = intPreferencesKey("water_max_servings")
        val TEA_MAX_SERVINGS = intPreferencesKey("tea_max_servings")
        val COFFEE_MAX_SERVINGS = intPreferencesKey("coffee_max_servings")
        val ANALYTICS_START_DATE = stringPreferencesKey("analytics_start_date")
        val SELECTED_CHART_MODE = stringPreferencesKey("selected_chart_mode")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            dailyGoalMl = prefs[Keys.DAILY_GOAL_ML] ?: 2000,
            waterServingMl = prefs[Keys.WATER_SERVING_ML] ?: 500,
            teaServingMl = prefs[Keys.TEA_SERVING_ML] ?: 350,
            coffeeServingMl = prefs[Keys.COFFEE_SERVING_ML] ?: 350,
            waterMaxServings = prefs[Keys.WATER_MAX_SERVINGS] ?: 0,
            teaMaxServings = prefs[Keys.TEA_MAX_SERVINGS] ?: 0,
            coffeeMaxServings = prefs[Keys.COFFEE_MAX_SERVINGS] ?: 0,
            analyticsStartDate = prefs[Keys.ANALYTICS_START_DATE]?.let {
                LocalDate.parse(it)
            },
            selectedChartMode = prefs[Keys.SELECTED_CHART_MODE]?.let {
                ChartMode.fromKey(it)
            } ?: ChartMode.BAR,
        )
    }

    suspend fun updateDailyGoal(goalMl: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_ML] = goalMl }
    }

    suspend fun updateServingSize(drinkType: DrinkType, sizeMl: Int) {
        context.dataStore.edit { prefs ->
            when (drinkType) {
                DrinkType.WATER -> prefs[Keys.WATER_SERVING_ML] = sizeMl
                DrinkType.TEA -> prefs[Keys.TEA_SERVING_ML] = sizeMl
                DrinkType.COFFEE -> prefs[Keys.COFFEE_SERVING_ML] = sizeMl
            }
        }
    }

    suspend fun updateAnalyticsStartDate(date: LocalDate?) {
        context.dataStore.edit { prefs ->
            if (date != null) {
                prefs[Keys.ANALYTICS_START_DATE] = date.toString()
            } else {
                prefs.remove(Keys.ANALYTICS_START_DATE)
            }
        }
    }

    suspend fun updateSelectedChartMode(mode: ChartMode) {
        context.dataStore.edit { it[Keys.SELECTED_CHART_MODE] = mode.key }
    }

    suspend fun updateMaxServings(drinkType: DrinkType, maxServings: Int) {
        context.dataStore.edit { prefs ->
            when (drinkType) {
                DrinkType.WATER -> prefs[Keys.WATER_MAX_SERVINGS] = maxServings
                DrinkType.TEA -> prefs[Keys.TEA_MAX_SERVINGS] = maxServings
                DrinkType.COFFEE -> prefs[Keys.COFFEE_MAX_SERVINGS] = maxServings
            }
        }
    }
}
