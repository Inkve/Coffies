package com.example.coffies.ui.settings

data class SettingsViewState(
    val isEditing: Boolean = false,
    val name: String = "",
    val cupGoal: Int? = null,
    val monthlyCupGoal: Int? = null,
    val spendGoal: Float? = null,
    val monthlySpendGoal: Float? = null
)
