package com.fluidis.app.core.model

/** One drink type's intake on one day. */
data class DailyDrinkTotal(
    val date: String,
    val drinkType: String,
    val total: Int,
)
