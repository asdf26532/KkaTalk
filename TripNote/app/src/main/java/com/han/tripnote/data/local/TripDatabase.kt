package com.han.tripnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.han.tripnote.data.local.dao.TripDao
import com.han.tripnote.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class],
    version = 1
)
abstract class TripDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getInstance(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}