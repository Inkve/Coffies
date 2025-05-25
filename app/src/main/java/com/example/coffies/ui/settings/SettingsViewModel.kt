package com.example.coffies.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffies.database.AppDatabase
import com.example.coffies.database.usersettings.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(SettingsViewState())
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()
    private var lastDbSettings: UserSettings? = null

    init {
        viewModelScope.launch {
            db.userSettingsDao().getSettings().collect { settings ->
                if (!_state.value.isEditing) {
                    lastDbSettings = settings
                    _state.value = SettingsViewState(
                        isEditing = false,
                        name = settings?.name.orEmpty(),
                        cupGoal = settings?.daily_cup_goal,
                        monthlyCupGoal = settings?.monthly_cup_goal,
                        spendGoal = settings?.daily_spend_goal,
                        monthlySpendGoal = settings?.monthly_spend_goal
                    )
                }
            }
        }
    }

    fun enterEditMode() {
        _state.value = _state.value.copy(isEditing = true)
    }

    fun exitEditMode() {
        _state.value = _state.value.copy(isEditing = false)
    }

    fun discardChangesAndExitEditMode() {
        val s = lastDbSettings
        _state.value = SettingsViewState(
            isEditing = false,
            name = s?.name.orEmpty(),
            cupGoal = s?.daily_cup_goal,
            monthlyCupGoal = s?.monthly_cup_goal,
            spendGoal = s?.daily_spend_goal,
            monthlySpendGoal = s?.monthly_spend_goal
        )
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onCupGoalChanged(value: String) {
        _state.value = _state.value.copy(cupGoal = value.toIntOrNull())
    }

    fun onMonthlyCupGoalChanged(value: String) {
        _state.value = _state.value.copy(monthlyCupGoal = value.toIntOrNull())
    }

    fun onSpendGoalChanged(value: String) {
        _state.value = _state.value.copy(spendGoal = value.toFloatOrNull())
    }

    fun onMonthlySpendGoalChanged(value: String) {
        _state.value = _state.value.copy(monthlySpendGoal = value.toFloatOrNull())
    }

    fun hasChanges(): Boolean {
        val s = lastDbSettings
        val curr = _state.value
        if (s == null && curr.name.isBlank() && curr.cupGoal == null && curr.monthlyCupGoal == null
            && curr.spendGoal == null && curr.monthlySpendGoal == null
        ) return false

        return curr.name != (s?.name.orEmpty()) ||
                curr.cupGoal != s?.daily_cup_goal ||
                curr.monthlyCupGoal != s?.monthly_cup_goal ||
                curr.spendGoal != s?.daily_spend_goal ||
                curr.monthlySpendGoal != s?.monthly_spend_goal
    }

    fun save() {
        val st = _state.value
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.userSettingsDao().insertOrUpdate(
                    UserSettings(
                        name = st.name,
                        daily_cup_goal = st.cupGoal,
                        monthly_cup_goal = st.monthlyCupGoal,
                        daily_spend_goal = st.spendGoal,
                        monthly_spend_goal = st.monthlySpendGoal
                    )
                )
            }
            exitEditMode()
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.userSettingsDao().clearSettings()
            }
            _state.value = SettingsViewState()
        }
    }
}
