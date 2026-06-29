package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.local.WeatherDao
import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.mapper.toEntity
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.mapper.toWeatherUiStateFromCache
import com.example.weatherforecastapp.ui.screen.WeatherUiState
import kotlinx.coroutines.flow.firstOrNull

// 仓库接口
interface WeatherRepository {
    suspend fun getWeather(locationId: String, apiKey: String): WeatherUiState
}

// 仓库实现类
class WeatherRepositoryImp(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override suspend fun getWeather(locationId: String, apiKey: String): WeatherUiState {
        return try {
            // 1. 尝试从网络获取
            val response: WeatherDto = apiService.get3dWeather(locationId, apiKey)
            val cityName = "上海" // 这里建议后续改为动态获取

            // 2. 网络成功，存入数据库（转换 DTO -> Entity）
            weatherDao.insertWeather(response.toEntity(cityName))

            // 3. 返回网络获取的成功状态
            response.toWeatherUiState(cityName)

        } catch (e: Exception) {
            // 4. 网络失败，尝试从本地数据库取出最新的一条记录
            // 使用 firstOrNull() 获取 Flow 发射的当前值
            val cachedEntity : WeatherEntity ?= weatherDao.getWeatherByCity("上海").firstOrNull()

            if (cachedEntity != null) {
                // 如果本地有缓存，转换并返回（Entity -> Success）
                cachedEntity.toWeatherUiStateFromCache()
            } else {
                // 如果本地也没数据，返回错误状态
                WeatherUiState.Error(message = "无网络连接且无本地缓存: ${e.localizedMessage}")
            }
        }
    }
}
