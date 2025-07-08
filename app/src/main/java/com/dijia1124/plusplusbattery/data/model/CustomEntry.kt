package com.dijia1124.plusplusbattery.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomEntry(
    val path: String,
    val title: String,
    val unit: String
)

