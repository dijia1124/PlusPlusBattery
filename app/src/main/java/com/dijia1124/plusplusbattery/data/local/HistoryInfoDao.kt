package com.dijia1124.plusplusbattery.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dijia1124.plusplusbattery.data.model.HistoryInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryInfoDao {
    @Query("SELECT * FROM HistoryInfo")
    fun getAllHistoryInfos(): Flow<List<HistoryInfo>>
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertHistoryInfo(subject: HistoryInfo)
    @Update
    suspend fun updateHistoryInfo(subject: HistoryInfo)
    @Delete
    suspend fun deleteHistoryInfo(subject: HistoryInfo)
    @Query("SELECT EXISTS(SELECT 1 FROM HistoryInfo WHERE dateString = :dateString LIMIT 1)")
    suspend fun existsHistoryInfo(dateString: String): Boolean
    @Query("SELECT * FROM historyinfo WHERE dateString = :dateString LIMIT 1")
    suspend fun getHistoryInfoByDate(dateString: String): HistoryInfo?

    @Query("SELECT * FROM HistoryInfo ORDER BY date ASC LIMIT 1")
    fun getFirstHistoryInfo(): Flow<HistoryInfo?>

    @Query("SELECT * FROM HistoryInfo ORDER BY date DESC LIMIT 1")
    fun getLastHistoryInfo(): Flow<HistoryInfo?>
}