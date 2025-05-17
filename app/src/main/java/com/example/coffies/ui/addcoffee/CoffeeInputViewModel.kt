package com.example.coffies.ui.addcoffee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffeetype.CoffeeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoffeeInputViewModel(
    private val db: AppDatabase
) : ViewModel() {

    val coffeeTypesFlow: Flow<List<CoffeeType>> = db.coffeeTypeDao().getAll()

    suspend fun getCoffeeTypesOnce(): List<CoffeeType> =
        withContext(Dispatchers.IO) { db.coffeeTypeDao().getAll().first() }

    fun insertEntry(
        entry: CoffeeEntry,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.coffeeEntryDao().insert(entry)
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}