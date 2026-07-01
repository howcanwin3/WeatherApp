package com.example.weatherforecastapp.ui.screen

sealed interface WeatherUiState {
    object Loading : WeatherUiState

    data class Success(
        val cityName: String,
        val currentTemperature: String,
        val weatherDescription: String,
        val forecastItems: List<ForecastItem>,
        val lastUpdatedText: String,
        val sourceLabel: String,
    ) : WeatherUiState

    data class Error(
        val message: String,
    ) : WeatherUiState
}

data class ForecastItem(
    val dayOfWeek: String,
    val weather: String,
    val temperatureRange: String,
)
