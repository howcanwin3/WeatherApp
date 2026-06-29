package com.example.weatherforecastapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName")
    // 返回 Flow 时，Room 会自动处理异步
    fun getWeatherByCity(cityName: String): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_cache")
    suspend fun clearWeatherCache()
}
