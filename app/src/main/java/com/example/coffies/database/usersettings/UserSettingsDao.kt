package com.example.coffies.database.usersettings
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings LIMIT 1")
    fun getSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettings): Long

    @Query("DELETE FROM user_settings")
    suspend fun clearSettings()

    @Query("SELECT * FROM user_settings LIMIT 1")
    suspend fun getSettingsOnce(): UserSettings?
}

