package com.example.coffies.database.usersettings
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?,
    val birth_date: String?,
    val daily_cup_goal: Int?,
    val daily_spend_goal: Float?,
    val avatarUri: String?
)

