package com.han.tripnote.data.local.dao

import androidx.room.*
import com.han.tripnote.data.local.entity.TripEntity

@Dao
interface TripDao {

    @Query("SELECT * FROM trip")
    suspend fun getAll(): List<TripEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(trip: TripEntity)

    @Delete
    suspend fun delete(trip: TripEntity)
}