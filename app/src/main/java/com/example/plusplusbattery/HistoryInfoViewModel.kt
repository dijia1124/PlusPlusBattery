package com.example.plusplusbattery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val cRepository: HistoryInfoRepository
    init{
        cRepository = HistoryInfoRepository(application)
    }
    val allHistoryInfos: Flow<List<HistoryInfo>> = cRepository.allHistoryInfos
    fun insertHistoryInfo(historyInfo: HistoryInfo) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.insert(historyInfo)
    }
    fun updateHistoryInfo(historyInfo: HistoryInfo) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.update(historyInfo)
    }
    fun deleteHistoryInfo(historyInfo: HistoryInfo) = viewModelScope.launch(Dispatchers.IO) {
        cRepository.delete(historyInfo)
    }

    suspend fun getHistoryInfoByDate(dateString: String): HistoryInfo? {
        return cRepository.getHistoryInfoByDate(dateString)
    }

    suspend fun insertOrUpdateHistoryInfo(newInfo: HistoryInfo) {
        val oldInfo = getHistoryInfoByDate(newInfo.dateString)
        if (oldInfo == null) {
            insertHistoryInfo(newInfo)
        } else {
            val oldCount = oldInfo.cycleCount.toIntOrNull() ?: -1
            val newCount = newInfo.cycleCount.toIntOrNull() ?: -1
            if (newCount > oldCount) {
                updateHistoryInfo(newInfo.copy(uid = oldInfo.uid))
            }
        }
    }
}