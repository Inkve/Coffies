package com.example.coffies.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

data class MainScreenStats(
    val cupsCount: Int,
    val caffeine: Int,
    val totalSpent: Double,
    val avgMood: Int,
    val recommendations: List<String>,
    val cupGoal: Int? = null,
    val spendGoal: Float? = null
)

class MainViewModel(private val db: AppDatabase) : ViewModel() {
    private val _stats = MutableStateFlow<MainScreenStats?>(null)
    val stats: StateFlow<MainScreenStats?> = _stats

    fun loadStatsForToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val cups = db.coffeeEntryDao().getCupsCountForDate(today)
            val caffeine = db.coffeeEntryDao().getTotalCaffeineForDate(today)
            val spent = db.coffeeEntryDao().getTotalSpentForDate(today)
            val moodRaw = db.moodEntryDao().getAverageMoodLevelForDate(today) ?: 0.0
            val avgMood = ceil(moodRaw).toInt().coerceAtLeast(0)
            val settings = db.userSettingsDao().getSettingsOnce()
            val cupGoal = settings?.daily_cup_goal
            val spendGoal = settings?.daily_spend_goal

            val recommendations = listOf(
                "Пейте больше воды между чашками кофе!",
                "Держите баланс: не превышайте дневную норму кофеина.",
                "Попробуйте новые сорта кофе для разнообразия."
            )

            _stats.value = MainScreenStats(
                cupsCount = cups,
                caffeine = caffeine.toInt(),
                totalSpent = spent,
                avgMood = avgMood,
                recommendations = recommendations,
                cupGoal = cupGoal,
                spendGoal = spendGoal
            )
        }
    }

}

