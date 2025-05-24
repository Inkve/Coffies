package com.example.coffies.ui.main

data class MainScreenStats(
    val cupsCount: Int? = null,
    val caffeine: Int? = null,
    val totalSpent: Double? = null,
    val avgMood: Int? = null,
    val recommendations: List<String> = emptyList(),
    val cupGoal: Int? = null,
    val spendGoal: Float? = null
)
