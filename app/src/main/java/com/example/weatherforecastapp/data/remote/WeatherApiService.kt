package com.example.weatherforecastapp.data.remote

import com.example.weatherforecastapp.data.remote.model.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService{
    @GET("v7/weather/3d")//网址后缀
    suspend fun getWeather(
        @Query("location") location : String,//@Query("location")意思是在网址上加上?location=
        @Query("key") apiKey : String//@Query("key")意思是在网址上加上&key=
    ) : WeatherDto
}