package com.example.plusplusbattery

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface HistoryInfoDao {
    @Query("SELECT * FROM HistoryInfo")
    fun getAllHistoryInfos(): Flow<List<HistoryInfo>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistoryInfo(subject: HistoryInfo)
    @Update
    suspend fun updateHistoryInfo(subject: HistoryInfo)
    @Delete
    suspend fun deleteHistoryInfo(subject: HistoryInfo)
    @Query("SELECT EXISTS(SELECT 1 FROM HistoryInfo WHERE dateString = :dateString LIMIT 1)")
    suspend fun existsHistoryInfo(dateString: String): Boolean
}