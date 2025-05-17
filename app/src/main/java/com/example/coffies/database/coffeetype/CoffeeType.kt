package com.example.coffies.database.coffeetype

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffee_types")
data class CoffeeType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val default_volume_ml: Int?,
    val default_caffeine_mg_ml: Float?
)