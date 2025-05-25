package com.example.coffies.ui.analitycs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class AnalyticsViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val today = LocalDate.now()
            val days = (0..29).map { today.minusDays((29 - it).toLong()) }
            val dayStrings = days.map { it.format(formatter) }

            val entries = db.coffeeEntryDao().getAllBetweenDates(dayStrings.first(), dayStrings.last())
            val moods = db.moodEntryDao().getAllBetweenDates(dayStrings.first(), dayStrings.last())
            val userSettings = db.userSettingsDao().getSettingsOnce()

            val caffeineByDay = dayStrings.map { date ->
                val sumCaffeine = entries.filter { it.date == date }.sumOf { it.caffeine_mg ?: 0.0 }
                date to sumCaffeine
            }
            val cupsByDay = dayStrings.map { date ->
                val sumCups = entries.filter { it.date == date }.sumOf { it.quantity }.toDouble()
                date to sumCups
            }
            val moneyByDay = dayStrings.map { date ->
                val sumMoney = entries.filter { it.date == date }.sumOf { it.price ?: 0.0 }
                date to sumMoney
            }
            val moodByDay = dayStrings.map { date ->
                val moodsForDay = moods.filter { it.date == date }
                val avgMood = if (moodsForDay.isNotEmpty()) moodsForDay.map { it.mood_level }.average() else 0.0
                date to avgMood
            }

            val caffeineAvgs = caffeineByDay.map { it.second }.filter { it > 0 }
            val caffeineAvgNum = if (caffeineAvgs.isNotEmpty()) caffeineAvgs.average() else 0.0
            val caffeineAvg = if (caffeineAvgNum > 0) "%.0f мг".format(caffeineAvgNum) else "-"
            val isCaffeineExceeded = caffeineAvgNum > 400
            val caffeineGoal = "400 мг"

            val cupsSumNum = cupsByDay.map { it.second }.filter { it > 0 }.sum()
            val cupsSum = if (cupsSumNum > 0) "%.0f".format(cupsSumNum) else "-"
            val cupsGoal = userSettings?.monthly_cup_goal?.takeIf { it > 0 }?.toString()
            val isCupsExceeded = userSettings?.monthly_cup_goal?.let { it > 0 && cupsSumNum > it } ?: false

            val moneySumNum = moneyByDay.map { it.second }.filter { it > 0 }.sum()
            val moneySum = if (moneySumNum > 0) "%.2f ₽".format(moneySumNum) else "-"
            val moneyGoal = userSettings?.monthly_spend_goal?.takeIf { it > 0f }?.let { "%.2f ₽".format(it) }
            val isMoneyExceeded = userSettings?.monthly_spend_goal?.let { it > 0f && moneySumNum > it } ?: false
            
            val moodAvgs = moodByDay.map { it.second }.filter { it > 0 }
            val moodAvg = if (moodAvgs.isNotEmpty()) ceil(moodAvgs.average()).toInt() else 0

            _state.value = AnalyticsState(
                caffeinePoints = caffeineByDay,
                caffeineAvg = caffeineAvg,
                caffeineGoal = caffeineGoal,
                isCaffeineExceeded = isCaffeineExceeded,
                cupsPoints = cupsByDay,
                cupsSum = cupsSum,
                cupsGoal = cupsGoal,
                isCupsExceeded = isCupsExceeded,
                moneyPoints = moneyByDay,
                moneySum = moneySum,
                moneyGoal = moneyGoal,
                isMoneyExceeded = isMoneyExceeded,
                moodPoints = moodByDay,
                moodAvg = moodAvg
            )
        }
    }
}
