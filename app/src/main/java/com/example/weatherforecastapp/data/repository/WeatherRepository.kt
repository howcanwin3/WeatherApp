package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.screen.WeatherUiState

interface WeatherRepository {
    suspend fun getWeather(locationId: String): WeatherUiState
}

class WeatherRepositoryImp(
    private val apiService: WeatherApiService
) : WeatherRepository {
    override suspend fun getWeather(locationId: String): WeatherUiState {
        return apiService.getWeather3D(
            location = locationId,
            apiKey = ""
        ).toWeatherUiState()
    }
}
