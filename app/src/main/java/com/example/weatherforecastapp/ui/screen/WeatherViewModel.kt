package com.example.weatherforecastapp.ui.screen

import WeatherRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherforecastapp.WeatherApplication
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepository : WeatherRepository ) : ViewModel() {
    
    //  给出合理的默认初始值，避免一上来就是空白
    private val _uiState = MutableStateFlow(
        WeatherUiState("加载中...", "--℃", "正在获取天气数据...", emptyList())
    )
    val uiState : StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(locationId : String, apiKey : String) {
        viewModelScope.launch {
            try {
                val dto = weatherRepository.getWeather(locationId, apiKey)
                _uiState.value = dto.toWeatherUiState()
            } catch (e: Exception) {
                e.printStackTrace()
                //  捕捉具体错误（例如 403 Forbidden），反馈到 UI 界面上，方便调试
                _uiState.value = _uiState.value.copy(
                    cityName = "加载失败",
                    currentTemperature = "--℃",
                    weatherDescription = "错误原因: ${e.message ?: "未知网络错误"}"
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                val weatherRepository = application.container.weatherRepository
                WeatherViewModel(weatherRepository = weatherRepository)
            }
        }
    }
}
