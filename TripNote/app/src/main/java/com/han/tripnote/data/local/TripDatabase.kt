package com.han.tripnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.han.tripnote.data.local.dao.TripDao
import com.han.tripnote.data.local.entity.TripEntity

@Database(
    entities = [TripEntity::class],
    version = 2
)
abstract class TripDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE trip ADD COLUMN imageUri TEXT"
                )
            }
        }

        @Volatile
        private var INSTANCE: TripDatabase? = null

        fun getInstance(context: Context): TripDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_db"
                ).addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}