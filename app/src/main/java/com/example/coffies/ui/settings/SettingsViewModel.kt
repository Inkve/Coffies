package com.example.coffies.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.usersettings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val db: AppDatabase) : ViewModel() {
    private val _settings = MutableStateFlow<UserSettings?>(null)
    val settings: StateFlow<UserSettings?> = _settings

    init {
        viewModelScope.launch {
            db.userSettingsDao().getSettings().collect { _settings.value = it }
        }
    }

    fun saveSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            db.userSettingsDao().insertOrUpdate(newSettings)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            db.userSettingsDao().clearSettings()
        }
    }
}
