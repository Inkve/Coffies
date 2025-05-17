package com.example.coffies.database.moodentry
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood_level: Int, // 1..5
    val date: String,
    val time: String,
    val comment: String?
)
