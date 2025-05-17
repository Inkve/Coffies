package com.example.coffies.ui.addmood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffemoodlink.CoffeeMoodLink
import com.example.coffies.database.moodentry.MoodEntry
import com.example.coffies.database.coffeetype.CoffeeTypeIdName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoodInputViewModel(
    private val db: AppDatabase
) : ViewModel() {

    // Получить последние приемы кофе (например, 20 штук)
    val lastCoffeeEntriesFlow: Flow<List<CoffeeEntry>> =
        db.coffeeEntryDao().getAllDescLimited(20)

    // Кэш для быстрых lookup названия типа кофе по id
    private val coffeeTypeNameMap = mutableMapOf<Int, String>()

    // Инициализация кэша типов кофе
    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val types: List<CoffeeTypeIdName> = db.coffeeTypeDao().getAllCoffeeTypeNames()
                val map = types.associate { it.id to it.name }
                coffeeTypeNameMap.putAll(map)
            } catch (e: Exception) {
                // В случае ошибки пусть кэш останется пустым, можно добавить логирование
            }
        }
    }

    fun getCoffeeTypeName(id: Int): String {
        return coffeeTypeNameMap[id] ?: "Тип $id"
    }

    // Сохранить запись настроения и связь с приемом кофе
    fun saveMoodWithCoffeeLink(
        moodLevel: Int,
        date: String,
        time: String,
        comment: String?,
        coffeeEntryId: Int,
        relationType: String, // "before and during" or "after"
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val moodEntry = MoodEntry(
                    mood_level = moodLevel,
                    date = date,
                    time = time,
                    comment = comment
                )
                val moodId = withContext(Dispatchers.IO) {
                    db.moodEntryDao().insert(moodEntry).toInt()
                }
                val link = CoffeeMoodLink(
                    coffee_entry_id = coffeeEntryId,
                    mood_entry_id = moodId,
                    relation_type = relationType
                )
                withContext(Dispatchers.IO) {
                    db.coffeeMoodLinkDao().insert(link)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}