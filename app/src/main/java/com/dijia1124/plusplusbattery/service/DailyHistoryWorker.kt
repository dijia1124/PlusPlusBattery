package com.dijia1124.plusplusbattery.service

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dijia1124.plusplusbattery.data.repository.HistoryInfoRepository
import com.dijia1124.plusplusbattery.data.util.saveCycleCountToHistory

class DailyHistoryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WorkManager", "Attempting to save cycle count from DailyHistoryWorker.")
        return try {
            val repository = HistoryInfoRepository(applicationContext as Application)
            saveCycleCountToHistory(applicationContext, repository)
            Log.d("WorkManager", "Successfully attempted to save daily history.")
            Result.success()
        } catch (e: Exception) {
            Log.e("WorkManager", "Error while performing daily history work.", e)
            Result.failure()
        }
    }
}
