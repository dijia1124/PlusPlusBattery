package com.example.plusplusbattery

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [HistoryInfo::class], version = 1, exportSchema = false)
abstract class HistoryInfoDatabase : RoomDatabase() {
    abstract fun historyInfoDao(): HistoryInfoDao
    companion object {
        @Volatile
        private var INSTANCE: HistoryInfoDatabase? = null
        fun getDatabase(context: Context): HistoryInfoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HistoryInfoDatabase::class.java,
                    "history_info_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}