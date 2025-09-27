package com.dijia1124.plusplusbattery.vm

import android.app.Application
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dijia1124.plusplusbattery.data.model.HistoryInfo
import com.dijia1124.plusplusbattery.data.repository.HistoryInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    suspend fun insertOrUpdateHistoryInfo(info: HistoryInfo) = cRepository.insertOrUpdate(info)

    fun getFirstHistoryInfo() = cRepository.getFirstHistoryInfo()

    fun getLastHistoryInfo() = cRepository.getLastHistoryInfo()

    suspend fun exportHistoryToCsv(context: Context) {
        withContext(Dispatchers.IO) {
            val historyList = allHistoryInfos.first()
            val csvBuilder = StringBuilder()
            csvBuilder.append("date,cycleCount\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            historyList.forEach { info ->
                csvBuilder.append("${sdf.format(Date(info.date))},${info.cycleCount}\n")
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "history_$timeStamp.csv"

            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(csvBuilder.toString().toByteArray())
                }
            }
        }
    }
}