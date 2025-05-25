package com.example.coffies.database.moodentry
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry): Long

    @Update
    suspend fun update(entry: MoodEntry)

    @Query("SELECT * FROM mood_entries ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getById(id: Int): MoodEntry?

    @Query("""
        SELECT * FROM mood_entries
        WHERE date >= :from AND date <= :to
        ORDER BY date DESC, time DESC
    """)
    suspend fun getEntriesForPeriod(
        from: String,
        to: String
    ): List<MoodEntry>

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :from AND :to")
    suspend fun getAllBetweenDates(from: String, to: String): List<MoodEntry>

    @Query("SELECT AVG(mood_level) FROM mood_entries WHERE date = :today")
    suspend fun getAverageMoodLevelForDate(today: String): Double?

    @Delete
    suspend fun delete(entry: MoodEntry)
}
