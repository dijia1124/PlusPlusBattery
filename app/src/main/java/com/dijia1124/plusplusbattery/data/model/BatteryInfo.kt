package com.dijia1124.plusplusbattery.data.model


data class BatteryInfo(
    val type: BatteryInfoType,
    val value: String,
    // Default is true, can be used to show key in notification-area monitor
    val isShowKeyInMonitor: Boolean = true,
)