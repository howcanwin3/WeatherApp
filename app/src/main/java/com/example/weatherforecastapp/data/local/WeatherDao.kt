package com.example.weatherforecastapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName LIMIT 1")
    fun getWeatherByCity(cityName: String): WeatherEntity?

    @Query("SELECT * FROM weather_cache WHERE cityName LIKE '%' || :keyword || '%' LIMIT 1")
    fun searchWeatherByKeyword(keyword: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_cache")
    fun clearWeatherCache(): Int
}
