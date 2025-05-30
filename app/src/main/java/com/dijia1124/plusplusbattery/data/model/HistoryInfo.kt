package com.dijia1124.plusplusbattery.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["dateString"], unique = true)])
data class HistoryInfo(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val date: Long,
    val dateString: String,
    val cycleCount: String
)