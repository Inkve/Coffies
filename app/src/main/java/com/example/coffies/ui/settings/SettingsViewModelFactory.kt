package com.example.coffies.ui.settings

import com.example.coffies.database.AppDatabase

class SettingsViewModelFactory(private val db: AppDatabase) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
