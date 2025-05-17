package com.example.coffies.database.coffeentry
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeEntryDao {

    @Query("SELECT * FROM coffee_entries ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<CoffeeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CoffeeEntry): Long

    @Update
    suspend fun update(entry: CoffeeEntry)

    @Delete
    suspend fun delete(entry: CoffeeEntry)

    @Query("SELECT * FROM coffee_entries WHERE id = :id")
    suspend fun getById(id: Int): CoffeeEntry?

    @Query("SELECT * FROM coffee_entries ORDER BY date DESC, time DESC LIMIT :limit")
    fun getAllDescLimited(limit: Int): Flow<List<CoffeeEntry>>

    @Query("SELECT * FROM coffee_entries WHERE date >= :from AND date <= :to ORDER BY date DESC, time DESC")
    suspend fun getEntriesForPeriod(
        from: String,
        to: String
    ): List<CoffeeEntry>

    @Query("SELECT coalesce(SUM(quantity), 0) FROM coffee_entries WHERE date = :today")
    suspend fun getCupsCountForDate(today: String): Int

    @Query("SELECT coalesce(SUM(coalesce(caffeine_mg,0) * quantity), 0) FROM coffee_entries WHERE date = :today")
    suspend fun getTotalCaffeineForDate(today: String): Double

    @Query("SELECT coalesce(SUM(coalesce(price,0)), 0) FROM coffee_entries WHERE date = :today")
    suspend fun getTotalSpentForDate(today: String): Double

}
