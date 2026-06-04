package com.example.weatherforecastapp.data.remote

import com.example.weatherforecastapp.data.remote.model.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    //1.指定请求方式为GET，并写上网址后缀“”
    @GET("v7/weather/3d")
    //2.告诉系统这是个耗时网络请求，请配合协程使用
    suspend fun getWeather3Days(
        @Query("location") location: String,
        @Query("key") apiKey: String
    ) : WeatherDto
}