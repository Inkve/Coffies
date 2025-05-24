package com.example.coffies.ui.coffeedetails

import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.moodentry.MoodEntry

data class CoffeeDetailsState(
    val coffee: CoffeeEntry? = null,
    val moodMap: Map<String, MoodEntry?> = emptyMap(),
    val coffeeTypeNames: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)