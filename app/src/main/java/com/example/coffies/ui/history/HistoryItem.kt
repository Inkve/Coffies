package com.example.coffies.ui.history

import java.time.LocalDate
import java.time.LocalDateTime

sealed class HistoryItem {
    data class DateHeader(val date: LocalDate) : HistoryItem()

    data class CoffeeItem(
        val id: Int,
        val type: String,
        val volume: Int?,
        val quantity: Int,
        val price: Double?,
        val dateTime: LocalDateTime,
        val place: String?,
        val comment: String?
    ) : HistoryItem()

    data class MoodItem(
        val id: Int,
        val level: Int,
        val dateTime: LocalDateTime,
        val comment: String?
    ) : HistoryItem()
}

data class HistoryFilters(
    val showCoffee: Boolean = true,
    val showMood: Boolean = true
)
