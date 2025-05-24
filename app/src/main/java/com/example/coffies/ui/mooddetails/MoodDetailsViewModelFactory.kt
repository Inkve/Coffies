package com.example.coffies.ui.mooddetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coffies.database.AppDatabase

class MoodDetailsViewModelFactory(
    private val db: AppDatabase,
    private val moodEntryId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodDetailsViewModel(db, moodEntryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
