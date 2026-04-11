package com.fluidis.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DrinkTypeTest {

    @Test
    fun `fromKey returns correct type`() {
        assertEquals(DrinkType.WATER, DrinkType.fromKey("water"))
        assertEquals(DrinkType.TEA, DrinkType.fromKey("tea"))
        assertEquals(DrinkType.COFFEE, DrinkType.fromKey("coffee"))
    }

    @Test(expected = NoSuchElementException::class)
    fun `fromKey throws for unknown key`() {
        DrinkType.fromKey("juice")
    }

    @Test
    fun `ChartMode fromKey returns correct mode`() {
        assertEquals(ChartMode.BAR, ChartMode.fromKey("bar"))
        assertEquals(ChartMode.STACKED_BAR, ChartMode.fromKey("stacked_bar"))
        assertEquals(ChartMode.LINE, ChartMode.fromKey("line"))
        assertEquals(ChartMode.AREA, ChartMode.fromKey("area"))
    }

    @Test
    fun `ChartMode fromKey defaults to BAR for unknown`() {
        assertEquals(ChartMode.BAR, ChartMode.fromKey("unknown"))
    }
}
