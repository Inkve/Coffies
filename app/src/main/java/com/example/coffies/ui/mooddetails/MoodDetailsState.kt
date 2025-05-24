package com.example.coffies.ui.mooddetails

import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.moodentry.MoodEntry

data class MoodDetailsState(
    val mood: MoodEntry? = null,
    val coffee: CoffeeEntry? = null,
    val coffeeTypeName: String = "",
    val relationType: String = "",
    val loading: Boolean = true,
    val error: String? = null
)