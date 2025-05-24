package com.example.coffies.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.moodentry.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val _dateRange = MutableStateFlow(LocalDate.now().minusDays(7) to LocalDate.now())
    val dateRange: StateFlow<Pair<LocalDate, LocalDate>> = _dateRange.asStateFlow()

    private val _filters = MutableStateFlow(HistoryFilters())
    val filters: StateFlow<HistoryFilters> = _filters.asStateFlow()

    private val reloadTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_dateRange, _filters) { range, filters -> range to filters }
                .flatMapLatest { (range, filters) ->
                    reloadTrigger.onStart { emit(Unit) }.map {
                        _isLoading.value = true
                        val items = loadGroupedHistoryItems(range, filters)
                        _isLoading.value = false
                        items
                    }
                }
                .flowOn(Dispatchers.IO)
                .catch {
                    _isLoading.value = false
                    _historyItems.value = emptyList()
                }
                .collect {
                    _historyItems.value = it
                }
        }
    }

    fun updateDateRange(from: LocalDate, to: LocalDate) {
        _dateRange.value = from to to
        forceReload()
    }

    fun toggleCoffeeFilter(show: Boolean) {
        _filters.value = _filters.value.copy(showCoffee = show)
        forceReload()
    }
    fun toggleMoodFilter(show: Boolean) {
        _filters.value = _filters.value.copy(showMood = show)
        forceReload()
    }

    fun forceReload() {
        reloadTrigger.tryEmit(Unit)
    }

    private suspend fun loadGroupedHistoryItems(
        dateRange: Pair<LocalDate, LocalDate>,
        filters: HistoryFilters
    ): List<HistoryItem> {
        val (start, end) = dateRange
        val startStr = start.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val endStr = end.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val coffeeTypeMap = try {
            db.coffeeTypeDao()
                .getAllCoffeeTypeNames()
                .associate { it.id to it.name }
        } catch (_: Exception) {
            emptyMap()
        }

        val coffee = if (filters.showCoffee) {
            try {
                db.coffeeEntryDao().getEntriesForPeriod(startStr, endStr)
                    .map { it.toHistoryItem(coffeeTypeMap) }
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()

        val mood = if (filters.showMood) {
            try {
                db.moodEntryDao().getEntriesForPeriod(startStr, endStr)
                    .map { it.toHistoryItem() }
            } catch (_: Exception) {
                emptyList()
            }
        } else emptyList()

        val allItems = (coffee + mood).sortedByDescending {
            when (it) {
                is HistoryItem.CoffeeItem -> it.dateTime
                is HistoryItem.MoodItem -> it.dateTime
                is HistoryItem.DateHeader -> LocalDateTime.MIN
            }
        }

        return allItems
            .groupBy {
                when (it) {
                    is HistoryItem.CoffeeItem -> it.dateTime.toLocalDate()
                    is HistoryItem.MoodItem -> it.dateTime.toLocalDate()
                    is HistoryItem.DateHeader -> it.date
                }
            }
            .toSortedMap(compareByDescending { it })
            .flatMap { (date, items) ->
                listOf(HistoryItem.DateHeader(date)) + items
            }
    }

    private fun CoffeeEntry.toHistoryItem(coffeeTypeMap: Map<Int, String>): HistoryItem.CoffeeItem {
        return HistoryItem.CoffeeItem(
            id = id,
            type = coffeeTypeMap[coffee_type_id] ?: "Неизвестно",
            volume = volume_ml,
            quantity = quantity,
            price = price,
            dateTime = LocalDateTime.parse("${date}T${time}"),
            place = place,
            comment = comment
        )
    }

    private fun MoodEntry.toHistoryItem(): HistoryItem.MoodItem {
        return HistoryItem.MoodItem(
            id = id,
            level = mood_level,
            dateTime = LocalDateTime.parse("${date}T${time}"),
            comment = comment
        )
    }
}
