package com.example.coffies.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coffies.database.coffeentry.CoffeeEntry
import com.example.coffies.database.coffeentry.CoffeeEntryDao
import com.example.coffies.database.coffemoodlink.CoffeeMoodLink
import com.example.coffies.database.coffemoodlink.CoffeeMoodLinkDao
import com.example.coffies.database.coffeetype.CoffeeType
import com.example.coffies.database.coffeetype.CoffeeTypeDao
import com.example.coffies.database.moodentry.MoodEntry
import com.example.coffies.database.moodentry.MoodEntryDao
import com.example.coffies.database.usersettings.UserSettings
import com.example.coffies.database.usersettings.UserSettingsDao

@Database(
    entities = [
        CoffeeType::class,
        CoffeeEntry::class,
        MoodEntry::class,
        CoffeeMoodLink::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun coffeeTypeDao(): CoffeeTypeDao
    abstract fun coffeeEntryDao(): CoffeeEntryDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun coffeeMoodLinkDao(): CoffeeMoodLinkDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffee_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}