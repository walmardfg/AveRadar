package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [BirdEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BirdDatabase : RoomDatabase() {
    abstract fun birdDao(): BirdDao

    companion object {
        @Volatile
        private var INSTANCE: BirdDatabase? = null

        fun getInstance(context: Context): BirdDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirdDatabase::class.java,
                    "avradar_birds.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
