package com.example.coffies.ui.addcoffee

import com.example.coffies.database.coffeetype.CoffeeType

data class CoffeeInputFormState(
    val coffeeType: CoffeeType? = null,
    val volume: String = "",
    val quantity: String = "1",
    val caffeine: String = "",
    val date: String = "",
    val dateIso: String = "",
    val time: String = "",
    val cost: String = "",
    val place: String = "",
    val comment: String = "",
    val coffeeTypeError: String? = null,
    val volumeError: String? = null,
    val quantityError: String? = null,
    val caffeineError: String? = null,
    val costError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val success: Boolean = false,
    val errorMsg: String? = null
)