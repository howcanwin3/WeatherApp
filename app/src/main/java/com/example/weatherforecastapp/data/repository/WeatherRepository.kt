package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.remote.model.WeatherDto

class WeatherRepository (private val apiService: WeatherApiService){
    //声明一个挂起函数，在协程里跑耗时请求
    suspend fun getWeatherForecast(location :String ,apiKey : String): WeatherDto{
        return apiService.getWeather3Days( location, apiKey)
    }

}