package com.example.coffies.database.coffeentry
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.coffies.database.coffeetype.CoffeeType

@Entity(
    tableName = "coffee_entries",
    foreignKeys = [
        ForeignKey(
            entity = CoffeeType::class,
            parentColumns = ["id"],
            childColumns = ["coffee_type_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("coffee_type_id")]
)
data class CoffeeEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coffee_type_id: Int,
    val volume_ml: Int?,
    val quantity: Int = 1,
    val caffeine_mg: Double?,
    val price: Double?,
    val date: String,
    val time: String,
    val place: String?,
    val comment: String?
)

data class CaffeineDay(val date: String, val caffeine: Double)
data class CupsDay(val date: String, val cups: Int)
