package com.example.weatherforecastapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.weatherforecastapp.data.model.FavoriteCitySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName LIMIT 1")
    fun getWeatherByCity(cityName: String): WeatherEntity?

    @Query("SELECT * FROM weather_cache WHERE cityName LIKE '%' || :keyword || '%' LIMIT 1")
    fun searchWeatherByKeyword(keyword: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeather(weather: WeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoriteCity(city: FavoriteCityEntity)

    @Query("DELETE FROM favorite_cities WHERE cityId = :cityId")
    fun deleteFavoriteCity(cityId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE cityId = :cityId)")
    fun isFavoriteCity(cityId: String): Boolean

    @Query("UPDATE favorite_cities SET isDefault = 0")
    fun clearDefaultFavoriteCity()

    @Query("UPDATE favorite_cities SET isDefault = 1 WHERE cityId = :cityId")
    fun markDefaultFavoriteCity(cityId: String)

    @Query("SELECT cityId FROM favorite_cities WHERE isDefault = 1 LIMIT 1")
    fun getDefaultFavoriteCityId(): String?

    @Query(
        """
        SELECT cityId
        FROM favorite_cities
        ORDER BY isDefault DESC, addedAt DESC
        LIMIT 1
        """
    )
    fun getTopFavoriteCityId(): String?

    @Transaction
    fun replaceDefaultFavoriteCity(cityId: String) {
        clearDefaultFavoriteCity()
        markDefaultFavoriteCity(cityId)
    }

    @Query(
        """
        SELECT
            favorite_cities.cityId AS cityId,
            favorite_cities.cityName AS cityName,
            favorite_cities.regionText AS regionText,
            favorite_cities.displayName AS displayName,
            weather_cache.currentTemperature AS currentTemperature,
            weather_cache.description AS description,
            weather_cache.highTemperature AS highTemperature,
            weather_cache.lowTemperature AS lowTemperature,
            weather_cache.lastUpdated AS lastUpdated,
            favorite_cities.isDefault AS isDefault
        FROM favorite_cities
        LEFT JOIN weather_cache
            ON favorite_cities.displayName = weather_cache.cityName
        ORDER BY favorite_cities.isDefault DESC, favorite_cities.addedAt DESC
        """
    )
    fun observeFavoriteCitySummaries(): Flow<List<FavoriteCitySummary>>

    @Query(
        """
        SELECT
            favorite_cities.cityId AS cityId,
            favorite_cities.cityName AS cityName,
            favorite_cities.regionText AS regionText,
            favorite_cities.displayName AS displayName,
            weather_cache.currentTemperature AS currentTemperature,
            weather_cache.description AS description,
            weather_cache.highTemperature AS highTemperature,
            weather_cache.lowTemperature AS lowTemperature,
            weather_cache.lastUpdated AS lastUpdated,
            favorite_cities.isDefault AS isDefault
        FROM favorite_cities
        LEFT JOIN weather_cache
            ON favorite_cities.displayName = weather_cache.cityName
        WHERE favorite_cities.isDefault = 1
        LIMIT 1
        """
    )
    fun getDefaultFavoriteCitySummary(): FavoriteCitySummary?

    @Query("DELETE FROM weather_cache")
    fun clearWeatherCache(): Int
}
