package com.example.coffies.ui.coffeetype

data class CoffeeTypeFormState(
    val nameError: String? = null,
    val volumeError: String? = null,
    val caffeineError: String? = null,
    val success: Boolean = false
)