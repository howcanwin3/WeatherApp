package com.example.weatherforecastapp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto (
    @SerialName("code") val code : String,
    @SerialName("daily") val dailyList : List<DailyDto>
)

@Serializable
data class DailyDto(
    @SerialName("fxDate") val fxDate : String,
    @SerialName("tempMax") val tempMax : String,
    @SerialName("tempMin") val tempMin : String,
    @SerialName("textDay") val textDay : String,
)
