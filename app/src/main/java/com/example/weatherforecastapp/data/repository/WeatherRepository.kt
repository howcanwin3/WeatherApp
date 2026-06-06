package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.remote.WeatherApi
import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.screen.WeatherUiState

interface WeatherRepository {
    // 修复：增加 locationId 参数
    suspend fun getWeather(locationId: String): WeatherUiState
}

class WeatherRepositoryImp(
    private val apiService: WeatherApiService
) : WeatherRepository {
    override suspend fun getWeather(locationId: String): WeatherUiState {
        // 调用 ApiService 并完成 Dto 到 UiState 的转换
        return apiService.getWeather3Days(
            location = locationId,
            apiKey = "a66369e0e13b4ef7837beb5437dbfdf0" // 替换为你的真实 Key
        ).toWeatherUiState()
    }
}
