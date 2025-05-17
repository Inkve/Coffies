package com.example.coffies.database.coffemoodlink
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.moodentry.MoodEntry

@Entity(
    tableName = "coffee_mood_links",
    foreignKeys = [
        ForeignKey(
            entity = CoffeeEntry::class,
            parentColumns = ["id"],
            childColumns = ["coffee_entry_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MoodEntry::class,
            parentColumns = ["id"],
            childColumns = ["mood_entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("coffee_entry_id"),
        Index("mood_entry_id")
    ]
)
data class CoffeeMoodLink(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coffee_entry_id: Int,
    val mood_entry_id: Int,
    val relation_type: String // 'before and during', 'after'
)
