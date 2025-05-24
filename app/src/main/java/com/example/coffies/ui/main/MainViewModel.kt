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

class MainViewModel(private val db: AppDatabase) : ViewModel() {
    private val _stats = MutableStateFlow<MainScreenStats?>(null)
    val stats: StateFlow<MainScreenStats?> = _stats

    fun loadStatsForToday() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            // Получаем данные только за сегодня
            val cups = db.coffeeEntryDao().getCupsCountForDate(today)
            val caffeine = db.coffeeEntryDao().getTotalCaffeineForDate(today)
            val spent = db.coffeeEntryDao().getTotalSpentForDate(today)
            val moodRaw = db.moodEntryDao().getAverageMoodLevelForDate(today) ?: 0.0
            val avgMood = ceil(moodRaw).toInt().takeIf { it > 0 }

            // Цели пользователя
            val settings = db.userSettingsDao().getSettingsOnce()
            val cupGoal = settings?.daily_cup_goal
            val spendGoal = settings?.daily_spend_goal

            // В будущем будут рекомендации, сейчас пусть пусто
            val recommendations = emptyList<String>()

            // Заполняем состояние
            _stats.value = MainScreenStats(
                cupsCount = if (cups == 0) null else cups,
                caffeine = if (caffeine == 0.0) null else caffeine.toInt(),
                totalSpent = if (spent == 0.0) null else spent,
                avgMood = avgMood,
                recommendations = recommendations,
                cupGoal = cupGoal,
                spendGoal = spendGoal
            )
        }
    }
}
