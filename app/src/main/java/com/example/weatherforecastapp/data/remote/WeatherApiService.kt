package com.example.weatherforecastapp.data.remote

import com.example.weatherforecastapp.data.remote.model.CityLookupDto
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("geo/v2/city/lookup")
    suspend fun lookupCity(
        @Query("location") location: String,
        @Query("key") apiKey: String,
        @Query("number") number: Int = 1,
        @Query("range") range: String = "cn"
    ): CityLookupDto

    @GET("v7/weather/3d")
    suspend fun get3dWeather(
        @Query("location") location: String,
        @Query("key") apiKey: String
    ): WeatherDto
}
