package com.fluidis.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsTest {

    @Test
    fun `servingMlFor returns correct values`() {
        val settings = Settings(
            waterServingMl = 600,
            teaServingMl = 400,
            coffeeServingMl = 300,
        )
        assertEquals(600, settings.servingMlFor(DrinkType.WATER))
        assertEquals(400, settings.servingMlFor(DrinkType.TEA))
        assertEquals(300, settings.servingMlFor(DrinkType.COFFEE))
    }

    @Test
    fun `defaults are correct`() {
        val settings = Settings()
        assertEquals(2000, settings.dailyGoalMl)
        assertEquals(500, settings.waterServingMl)
        assertEquals(350, settings.teaServingMl)
        assertEquals(350, settings.coffeeServingMl)
        assertEquals(null, settings.analyticsStartDate)
        assertEquals(ChartMode.BAR, settings.selectedChartMode)
    }
}
