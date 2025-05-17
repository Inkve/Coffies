package com.example.coffies.ui.addcoffee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffies.database.AppDatabase

class CoffeeInputViewModelFactory(
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoffeeInputViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoffeeInputViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}