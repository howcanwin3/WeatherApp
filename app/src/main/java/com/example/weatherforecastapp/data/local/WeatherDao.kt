package com.example.weatherforecastapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface WeatherDao {
    //@Query注释，为Query添加一个String参数，该参数是一个SQLite查询，用于从weather_cache表中检索指定城市的天气信息。
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName")
    //使用Flow作为返回值类型，通过数据库数据变化的通知来保持数据的最新状态
    suspend fun getWeatherByCity(cityName: String): Flow<WeatherEntity?>
    //插入天气信息(主键冲突策略为替换)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)
    //清除缓存
    @Query("DELETE FROM weather_cache ")
    suspend fun clearWeatherCache()
}