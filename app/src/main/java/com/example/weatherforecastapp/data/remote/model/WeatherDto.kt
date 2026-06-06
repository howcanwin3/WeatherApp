package com.example.weatherforecastapp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecastDto(
    @SerialName("code") val code : String, //code->状态码
    @SerialName("daily") val dailyList : List<DailyForecastDto>,//列表里面存的是DailyForecastDto->未来几天的天气
)
@Serializable
data class DailyForecastDto(
    @SerialName("fxDate") val fxDate : String,
    @SerialName("tempMax") val tempMax : String,
    @SerialName("tempMin") val tempMin : String,
    @SerialName("description") val description : String,
)

