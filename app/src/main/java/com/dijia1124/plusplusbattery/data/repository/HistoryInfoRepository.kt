package com.dijia1124.plusplusbattery.data.repository

import android.app.Application
import com.dijia1124.plusplusbattery.data.local.HistoryInfoDao
import com.dijia1124.plusplusbattery.data.local.HistoryInfoDatabase
import com.dijia1124.plusplusbattery.data.model.HistoryInfo
import kotlinx.coroutines.flow.Flow

class HistoryInfoRepository (application: Application) {
    private var historyInfoDao: HistoryInfoDao =
        HistoryInfoDatabase.Companion.getDatabase(application).historyInfoDao()
    val allHistoryInfos: Flow<List<HistoryInfo>> = historyInfoDao.getAllHistoryInfos()
    suspend fun insert(historyInfo: HistoryInfo) {
        historyInfoDao.insertHistoryInfo(historyInfo)
    }
    suspend fun delete(historyInfo: HistoryInfo) {
        historyInfoDao.deleteHistoryInfo(historyInfo)
    }
    suspend fun update(historyInfo: HistoryInfo) {
        historyInfoDao.updateHistoryInfo(historyInfo)
    }
    suspend fun getHistoryInfoByDate(dateString: String): HistoryInfo? {
        return historyInfoDao.getHistoryInfoByDate(dateString)
    }

    suspend fun insertOrUpdate(info: HistoryInfo) {
        val old = getHistoryInfoByDate(info.dateString)
        if (old == null) {
            insert(info)
        } else {
            val oldCnt = old.cycleCount.toIntOrNull() ?: -1
            val newCnt = info.cycleCount.toIntOrNull() ?: -1
            if (newCnt > oldCnt) {
                update(info.copy(uid = old.uid))
            }
        }
    }
}