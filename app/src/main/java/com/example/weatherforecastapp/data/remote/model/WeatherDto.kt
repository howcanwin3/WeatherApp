package com.example.weatherforecastapp.data.remote.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
//Serializable数据类-->用于接收服务器返回的JSON数据 value = "code" code是服务器返回的字段
@Serializable
data class WeatherDto (
    @SerializedName("code") val code : String,
    @SerializedName(value = "daily") val dailyList : List<DailyDto>//新建一个属性dailyList-->新建一个data class 用于指定列表数据类型
    )

@Serializable
data class DailyDto(
    @SerializedName("fxDate") val fxDate : String,
    @SerializedName("tempMax") val tempMax : String,
    @SerializedName("tempMin") val tempMin : String,
    @SerializedName("textDay") val textDay : String,
)