package com.example.coffies.ui.mooddetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MoodDetailsViewModel(
    private val db: AppDatabase,
    private val moodEntryId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(MoodDetailsState())
    val state: StateFlow<MoodDetailsState> = _state.asStateFlow()

    init {
        loadMoodDetails()
    }

    private fun loadMoodDetails() {
        viewModelScope.launch {
            try {
                val mood = db.moodEntryDao().getById(moodEntryId)
                if (mood == null) {
                    _state.value = MoodDetailsState(error = "Запись не найдена", loading = false)
                    return@launch
                }
                val link = db.coffeeMoodLinkDao().getLinksForMoodEntry(mood.id).firstOrNull()
                val coffee = link?.let { db.coffeeEntryDao().getById(it.coffee_entry_id) }
                val typeName = coffee?.let {
                    db.coffeeTypeDao().getAllCoffeeTypeNames().find { t -> t.id == coffee.coffee_type_id }?.name ?: ""
                } ?: ""
                _state.value = MoodDetailsState(
                    mood = mood,
                    coffee = coffee,
                    coffeeTypeName = typeName,
                    relationType = link?.relation_type ?: "",
                    loading = false
                )
            } catch (e: Exception) {
                _state.value = MoodDetailsState(error = e.message ?: "Ошибка", loading = false)
            }
        }
    }

    fun deleteMood(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val st = _state.value
            val mood = st.mood ?: return@launch
            db.moodEntryDao().delete(mood)
            onDeleted()
        }
    }

    fun getLinkedCoffeeInfo(): String {
        val st = _state.value
        val coffee = st.coffee ?: return "—"
        val typeName = st.coffeeTypeName.ifEmpty { "—" }
        val volume = coffee.volume_ml
        val time = coffee.time
        val date = try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(coffee.date)
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsed!!)
        } catch (e: Exception) {
            coffee.date
        }
        val moment = when (st.relationType) {
            "before and during" -> "до/во время"
            "after" -> "после"
            else -> ""
        }
        return "$typeName • $volume мл • $time $date • $moment"
    }

    fun moodLevelToText(level: Int): String = when (level) {
        1 -> "Очень плохо"
        2 -> "Плохо"
        3 -> "Нормально"
        4 -> "Хорошо"
        5 -> "Отлично"
        else -> "—"
    }

    fun moodLevelToDrawable(level: Int): Int = when (level) {
        1 -> com.example.coffies.R.drawable.mood_1
        2 -> com.example.coffies.R.drawable.mood_2
        3 -> com.example.coffies.R.drawable.mood_3
        4 -> com.example.coffies.R.drawable.mood_4
        5 -> com.example.coffies.R.drawable.mood_5
        else -> com.example.coffies.R.drawable.mood_3
    }

    fun formatDate(iso: String): String {
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso)
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsed!!)
        } catch (e: Exception) {
            iso
        }
    }
}
