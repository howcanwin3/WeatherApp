package com.example.weatherforecastapp.ui.screen


sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(
    val cityName: String,
    val currentTemperature: String,
    val weatherDescription: String,
    val forecastItems: List<ForecastItem>,//表示列表里面存的都是 ForecastItem类 的数据
    ): WeatherUiState

    data class Error(
        val message: String,
    ):WeatherUiState
}
data class ForecastItem(
    val dayOfWeek: String,
    val weather: String,
    val temperatureRange: String,
)
