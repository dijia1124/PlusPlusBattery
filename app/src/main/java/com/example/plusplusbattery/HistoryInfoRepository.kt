package com.example.plusplusbattery

import android.app.Application
import kotlinx.coroutines.flow.Flow

class HistoryInfoRepository (application: Application) {
    private var historyInfoDao: HistoryInfoDao =
        HistoryInfoDatabase.getDatabase(application).historyInfoDao()
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
}