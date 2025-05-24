package com.example.coffies.ui.addmood

import com.example.coffies.database.coffeentry.CoffeeEntry

data class MoodInputFormState(
    val moodLevel: Int = 0,
    val momentType: MoodMomentType? = MoodMomentType.BEFORE,
    val relatedCoffeeId: Int? = null,
    val relatedCoffeeTime: String? = null,
    val relatedCoffeeDate: String? = null,
    val coffeeList: List<CoffeeEntry> = emptyList(),
    val coffeeTypeNameMap: Map<Int, String> = emptyMap(),
    val date: String = "",
    val displayDate: String = "",
    val time: String = "",
    val comment: String = "",
    val moodError: String? = null,
    val momentError: String? = null,
    val relatedCoffeeError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val success: Boolean = false
)