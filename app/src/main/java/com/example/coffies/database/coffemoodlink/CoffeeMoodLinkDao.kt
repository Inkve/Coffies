package com.example.coffies.database.coffemoodlink
import androidx.room.*

@Dao
interface CoffeeMoodLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: CoffeeMoodLink): Long

    @Query("SELECT * FROM coffee_mood_links WHERE coffee_entry_id = :coffeeEntryId")
    suspend fun getLinksForCoffeeEntry(coffeeEntryId: Int): List<CoffeeMoodLink>

    @Query("SELECT * FROM coffee_mood_links WHERE mood_entry_id = :moodEntryId")
    suspend fun getLinksForMoodEntry(moodEntryId: Int): List<CoffeeMoodLink>

    @Delete
    suspend fun delete(link: CoffeeMoodLink)
}
