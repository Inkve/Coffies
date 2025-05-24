// CoffeeDetailsViewModel.kt
package com.example.coffies.ui.coffeedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.moodentry.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoffeeDetailsViewModel(
    private val db: AppDatabase,
    private val coffeeEntryId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(CoffeeDetailsState())
    val state: StateFlow<CoffeeDetailsState> = _state

    init {
        loadAll()
    }

    fun loadAll() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val entry = withContext(Dispatchers.IO) { db.coffeeEntryDao().getById(coffeeEntryId) }
                val coffeeTypeNames = withContext(Dispatchers.IO) {
                    db.coffeeTypeDao().getAllCoffeeTypeNames().associate { it.id to it.name }
                }
                val moodLinks = withContext(Dispatchers.IO) { db.coffeeMoodLinkDao().getLinksForCoffeeEntry(coffeeEntryId) }
                val moodMap = mutableMapOf<String, MoodEntry?>()
                for (link in moodLinks) {
                    val mood = withContext(Dispatchers.IO) { db.moodEntryDao().getById(link.mood_entry_id) }
                    moodMap[link.relation_type] = mood
                }
                _state.value = CoffeeDetailsState(
                    isLoading = false,
                    coffee = entry,
                    moodMap = moodMap,
                    coffeeTypeNames = coffeeTypeNames
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteWithMoods(onDeleted: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val links = db.coffeeMoodLinkDao().getLinksForCoffeeEntry(coffeeEntryId)
                val moodIds = links.map { it.mood_entry_id }
                links.forEach { db.coffeeMoodLinkDao().delete(it) }
                moodIds.forEach { id ->
                    db.moodEntryDao().getById(id)?.let { db.moodEntryDao().delete(it) }
                }
                db.coffeeEntryDao().deleteById(coffeeEntryId)
            }
            onDeleted()
        }
    }
}
