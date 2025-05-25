package com.example.coffies.database.coffeetype

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeTypeDao {

    @Query("SELECT * FROM coffee_types")
    fun getAll(): Flow<List<CoffeeType>>

    @Query("SELECT * FROM coffee_types WHERE id = :id")
    suspend fun getById(id: Int): CoffeeType?

    @Query("SELECT id, name FROM coffee_types")
    suspend fun getAllCoffeeTypeNames(): List<CoffeeTypeIdName>

    @Query("SELECT COUNT(*) FROM coffee_types")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertAll(types: List<CoffeeType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coffeeType: CoffeeType): Long

    @Update
    suspend fun update(coffeeType: CoffeeType)

    @Delete
    suspend fun delete(coffeeType: CoffeeType)
}