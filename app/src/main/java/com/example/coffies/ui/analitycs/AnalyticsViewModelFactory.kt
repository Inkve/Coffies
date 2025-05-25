package com.example.coffies.ui.analitycs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnalyticsViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            val db = com.example.coffies.database.AppDatabase.getInstance(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
