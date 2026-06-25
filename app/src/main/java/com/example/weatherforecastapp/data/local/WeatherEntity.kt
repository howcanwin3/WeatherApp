package com.example.weatherforecastapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey
    val cityName: String,
    val currentTemperature: String,
    val description: String,
    val lastUpdated: Long = System.currentTimeMillis()
)


