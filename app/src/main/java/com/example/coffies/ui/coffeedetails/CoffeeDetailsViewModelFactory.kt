package com.example.coffies.ui.coffeedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffies.database.AppDatabase

class CoffeeDetailsViewModelFactory(
    private val db: AppDatabase,
    private val coffeeEntryId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoffeeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoffeeDetailsViewModel(db, coffeeEntryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
