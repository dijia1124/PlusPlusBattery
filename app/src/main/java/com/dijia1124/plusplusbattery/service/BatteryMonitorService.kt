package com.dijia1124.plusplusbattery.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.NotificationCompat
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.repository.BatteryInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatteryMonitorService : Service() {
    companion object {
        private const val ACTION_STOP = "com.dijia1124.plusplusbattery.ACTION_STOP"
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var batteryRepo: BatteryInfoRepository
    private val channelId = "battery_monitor"
    private val notifId = 1001
    private lateinit var notificationManager: NotificationManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefsRepo by lazy { PrefsRepository(applicationContext) }
    private var updateJob: Job? = null

    @Volatile private var currentIntervalMs: Int = 1000

    // BroadcastReceiver to pause/resume updates
    // Note: For ColorOS 15, auto-launch needs to be enabled for this app
    // to receive screen on/off events.
    // AOSP roms do not need extra setups.
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> updateJob?.cancel()
                Intent.ACTION_SCREEN_ON  ->
                    if (updateJob?.isActive != true) startUpdating()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            prefsRepo.refreshInterval
                .collect { newInterval ->
                    // update once the value changes
                    currentIntervalMs = newInterval
                }
        }
        // Register for screen on/off
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
        batteryRepo = BatteryInfoRepository(applicationContext)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            updateJob?.cancel()
            stopSelf()
            return START_NOT_STICKY
        }

        if (updateJob?.isActive == true) {
            return START_STICKY
        }

        startForeground(notifId, buildNotification(getString(R.string.initializing)))
        startUpdating()
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        updateJob?.cancel()
        unregisterReceiver(screenReceiver)
        serviceScope.cancel()
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
            .addAction(
                R.drawable.speed_24dp_1f1f1f_fill1_wght400_grad0_opsz24,
                getString(R.string.stop), stopPending)
            .setOngoing(true)            // ongoing notification
            .setOnlyAlertOnce(true)      // silently update the notification
            .build()
    }

    private suspend fun fetchBatteryStatus(): String = withContext(Dispatchers.IO) {
        val isRoot = prefsRepo.isRootModeFlow.first()
        val allInfos =
            if (isRoot) batteryRepo.getBasicBatteryInfo() + batteryRepo.getRootBatteryInfo() + batteryRepo.readCustomEntries()
            else batteryRepo.getBasicBatteryInfo() + batteryRepo.getNonRootVoltCurrPwr()
        val visibleTypes = prefsRepo.visibleEntriesFlow.first()
        val filtered = if (visibleTypes.isEmpty()) {
            allInfos
        } else {
            allInfos.filter { info ->
                info.type in visibleTypes
            }
        }
        filtered.joinToString("\n") { info ->
            // if this entry is from the root‐only list, show the short key;
            // otherwise always show the localized title
            val label = when {
                info.type == BatteryInfoType.CUSTOM -> info.customTitle ?: info.type.key
                info.isShowKeyInMonitor -> info.type.key
                else -> getString(info.type.titleRes)
            }
            "$label: ${info.value}"
        }
    }

    private fun startUpdating() {
        updateJob = scope.launch {
            while (isActive) {
//                Log.d("BatteryMonitorService", "Updating notification")
                val statusText = fetchBatteryStatus()
                withContext(Dispatchers.Main) {
                    notificationManager.notify(notifId, buildNotification(statusText))
                }
                delay(currentIntervalMs.toLong())
            }
        }
    }
}