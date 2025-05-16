package com.example.plusplusbattery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatteryMonitorService : Service() {

    private lateinit var batteryRepo: BatteryInfoRepository
    private val channelId = "battery_monitor"
    private val notifId = 1001
    private lateinit var notificationManager: NotificationManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        batteryRepo = BatteryInfoRepository(applicationContext)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(notifId, buildNotification(getString(R.string.initializing)))
        startUpdating()
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val chan = NotificationChannel(
            channelId,
            "Battery Monitor",
            NotificationManager.IMPORTANCE_LOW
        )
        chan.setShowBadge(false)
        notificationManager.createNotificationChannel(chan)
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_battery_saver_24)
            .setContentTitle(getString(R.string.battery_monitor))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content)) // allow more lines
            .setOngoing(true)            // ongoing notification
            .setOnlyAlertOnce(true)      // silently update the notification
            .build()
    }

    private fun updateNotification(content: String) {
        val notif = buildNotification(content)
        notificationManager.notify(notifId, notif)
    }

    private suspend fun fetchBatteryStatus(): String = withContext(Dispatchers.IO) {
        val infos: List<BatteryInfo> = batteryRepo.getRootBatteryInfo(1.0, 1)
        //todo: testing purpose
        //todo: use prefrepo

        infos.joinToString(separator = "\n") { "${it.title}: ${it.value}" }
    }

    private fun startUpdating() {
        scope.launch {
            while (isActive) {
                val statusText = fetchBatteryStatus()
                withContext(Dispatchers.Main) {
                    updateNotification(statusText)
                }
                delay(1000)
            }
        }
    }
    //todo: add a way to stop the service
    //todo: add prompt to disable battery optimization
}