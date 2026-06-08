package com.example.weatherforecastapp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Serializable数据类 --> 用于接收服务器返回的JSON数据
@Serializable
data class WeatherDto (
    @SerialName("code") val code : String,
    @SerialName("daily") val dailyList : List<DailyDto> // 将服务器返回的 "daily" 字段映射为 dailyList
)

@Serializable
data class DailyDto(
    @SerialName("fxDate") val fxDate : String,
    @SerialName("tempMax") val tempMax : String,
    @SerialName("tempMin") val tempMin : String,
    @SerialName("textDay") val textDay : String,
)
