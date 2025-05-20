package com.example.plusplusbattery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatteryMonitorService : Service() {
    companion object {
        private const val ACTION_STOP = "com.example.plusplusbattery.ACTION_STOP"
    }

    private lateinit var batteryRepo: BatteryInfoRepository
    private val channelId = "battery_monitor"
    private val notifId = 1001
    private lateinit var notificationManager: NotificationManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefsRepo by lazy { PrefsRepository(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        batteryRepo = BatteryInfoRepository(applicationContext)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            scope.cancel()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(notifId, buildNotification(getString(R.string.initializing)))
        startUpdating()
        return START_STICKY
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
        val stopIntent = Intent(this, BatteryMonitorService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_battery_saver_24)
            .setContentTitle(getString(R.string.battery_monitor))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content)) // allow more lines
            .addAction(R.drawable.speed_24dp_1f1f1f_fill1_wght400_grad0_opsz24,
                getString(R.string.stop), stopPending)
            .setOngoing(true)            // ongoing notification
            .setOnlyAlertOnce(true)      // silently update the notification
            .build()
    }

    private suspend fun fetchBatteryStatus(): String = withContext(Dispatchers.IO) {
        val isRoot = prefsRepo.isRootModeFlow.first()
        val allInfos    = if (isRoot) batteryRepo.getBasicBatteryInfo() + batteryRepo.getRootBatteryInfo()
        else batteryRepo.getBasicBatteryInfo() + batteryRepo.getNonRootVoltCurrPwr()
        val visibleEntries = prefsRepo.visibleEntriesFlow.first()
        val filtered = if (visibleEntries.isEmpty()) {
            allInfos
        } else {
            allInfos.filter { info ->
                visibleEntries.contains(info.title)
            }
        }
        filtered.joinToString("\n") { info ->
            val label = info.key ?: info.title
            "$label: ${info.value}"
        }
    }

    private fun startUpdating() {
        scope.launch {
            while (isActive) {
                Log.d("BatteryMonitorService", "Updating notification")
                val statusText = fetchBatteryStatus()
                withContext(Dispatchers.Main) {
                    notificationManager.notify(notifId, buildNotification(statusText))}
                delay(1000)
            }
        }
    }
    //todo: add prompt to disable battery optimization
    //todo: stop at background
}