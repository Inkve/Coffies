package com.example.coffies.ui.analitycs

data class AnalyticsState(
    val caffeinePoints: List<Pair<String, Double>> = emptyList(),
    val caffeineAvg: String = "-",
    val caffeineGoal: String? = null,
    val isCaffeineExceeded: Boolean = false,
    val cupsPoints: List<Pair<String, Double>> = emptyList(),
    val cupsSum: String = "-",
    val cupsGoal: String? = null,
    val isCupsExceeded: Boolean = false,
    val moneyPoints: List<Pair<String, Double>> = emptyList(),
    val moneySum: String = "-",
    val moneyGoal: String? = null,
    val isMoneyExceeded: Boolean = false,
    val moodPoints: List<Pair<String, Double>> = emptyList(),
    val moodAvg: Int = 0
)
