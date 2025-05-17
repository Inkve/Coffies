package com.example.coffies.ui.coffeetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffies.database.coffeetype.CoffeeTypeDao

class CoffeeTypeViewModelFactory(
    private val dao: CoffeeTypeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoffeeTypeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoffeeTypeViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}