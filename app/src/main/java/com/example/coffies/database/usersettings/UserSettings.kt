package com.example.coffies.database.usersettings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val name: String? = null,
    val daily_cup_goal: Int? = null,
    val monthly_cup_goal: Int? = null,
    val daily_spend_goal: Float? = null,
    val monthly_spend_goal: Float? = null
)
