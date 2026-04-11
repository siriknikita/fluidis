package com.fluidis.app.core.ui

fun String.isValidAmountInput(maxLength: Int = 5): Boolean =
    all { it.isDigit() } && length <= maxLength
