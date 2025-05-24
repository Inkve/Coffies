package com.example.coffies.database.usersettings

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: UserSettings)

    @Query("DELETE FROM user_settings WHERE id = 1")
    suspend fun clearSettings()

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): UserSettings?
}
