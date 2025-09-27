package com.dijia1124.plusplusbattery.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.dijia1124.plusplusbattery.MainApplication
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.repository.BatteryInfoRepository
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import com.dijia1124.plusplusbattery.ui.components.FloatingWindowContent
import com.dijia1124.plusplusbattery.ui.theme.PlusPlusBatteryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class FloatingWindowService : Service(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
    private lateinit var savedStateRegistryController: SavedStateRegistryController
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle get() = lifecycleOwner.lifecycle
    private val lifecycleOwner = LifecycleOwnerService()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var batteryRepo: BatteryInfoRepository
    private lateinit var prefsRepo: PrefsRepository

    private lateinit var _batteryInfoText: MutableStateFlow<String>
    private val batteryInfoText get() = _batteryInfoText.asStateFlow()

    private var updateJob: Job? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> updateJob?.cancel()
                Intent.ACTION_SCREEN_ON  ->
                    if (updateJob?.isActive != true) startUpdating()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        _batteryInfoText = MutableStateFlow(getString(R.string.initializing))
        savedStateRegistryController = SavedStateRegistryController.create(this)
        savedStateRegistryController.performRestore(null)
        lifecycleOwner.create()
        batteryRepo = BatteryInfoRepository(applicationContext)
        prefsRepo = PrefsRepository(applicationContext)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Avoid creating multiple windows
        if (::floatingView.isInitialized) {
            return START_NOT_STICKY
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        floatingView = ComposeView(this).apply {
            setViewTreeViewModelStoreOwner(this@FloatingWindowService)
            setViewTreeLifecycleOwner(lifecycleOwner)

            setContent {
                val text by batteryInfoText.collectAsState()
                val useDarkTheme = (application as MainApplication).useDarkTheme
                val alpha by prefsRepo.floatingWindowAlpha.collectAsState(initial = 1.0f)
                val size by prefsRepo.floatingWindowSize.collectAsState(initial = 1.0f)
                val touchable by prefsRepo.floatingWindowTouchable.collectAsState(initial = true)
                val textColorKey by prefsRepo.floatingWindowTextColor.collectAsState(initial = "default")

                LaunchedEffect(touchable) {
                    val newFlags = if (touchable) {
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    } else {
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    }
                    params.flags = newFlags
                    windowManager.updateViewLayout(floatingView, params)
                }

                PlusPlusBatteryTheme(darkTheme = useDarkTheme, enableStatusBarEffect = false) {
                    val textColor = when (textColorKey) {
                        "primary" -> MaterialTheme.colorScheme.primary
                        "secondary" -> MaterialTheme.colorScheme.secondary
                        "error" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    FloatingWindowContent(
                        text = text,
                        alpha = alpha,
                        size = size,
                        textColor = textColor
                    ) {
                        dragAmount ->
                        if (touchable) {
                            params.x += dragAmount.x.roundToInt()
                            params.y += dragAmount.y.roundToInt()
                            windowManager.updateViewLayout(floatingView, params)
                        }
                    }
                }
            }
            setViewTreeSavedStateRegistryOwner(this@FloatingWindowService)
        }

        lifecycleOwner.start()
        lifecycleOwner.resume()

        windowManager.addView(floatingView, params)

        startUpdating()

        return START_STICKY
    }

    private fun startUpdating() {
        updateJob = serviceScope.launch {
            val interval = prefsRepo.refreshInterval.first()
            while (isActive) {
                _batteryInfoText.value = fetchBatteryInfo()
                delay(interval.toLong())
            }
        }
    }

    private suspend fun fetchBatteryInfo(): String = withContext(Dispatchers.IO) {
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

        if (filtered.isEmpty()) return@withContext ""

        return@withContext filtered.joinToString("\n") { info ->
            val label = when {
                info.type == BatteryInfoType.CUSTOM -> info.customTitle ?: info.type.key
                info.isShowKeyInMonitor -> info.type.key
                else -> getString(info.type.titleRes)
            }
            "$label: ${info.value}"
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        lifecycleOwner.destroy()
        unregisterReceiver(screenReceiver)
    }
}
