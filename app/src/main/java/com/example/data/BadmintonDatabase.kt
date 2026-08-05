package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BadmintonDao
import com.example.data.entity.MatchEntity
import com.example.data.entity.PlayerEntity

@Database(
    entities = [PlayerEntity::class, MatchEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BadmintonDatabase : RoomDatabase() {

    abstract fun badmintonDao(): BadmintonDao

    companion object {
        @Volatile
        private var INSTANCE: BadmintonDatabase? = null

        fun getDatabase(context: Context): BadmintonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BadmintonDatabase::class.java,
                    "badminton_stats_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
