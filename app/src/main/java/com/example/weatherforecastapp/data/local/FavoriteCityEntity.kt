package com.example.weatherforecastapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey
    val cityId: String,
    val cityName: String,
    val regionText: String,
    val displayName: String,
    val isDefault: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
