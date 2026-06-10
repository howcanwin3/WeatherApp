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
//companion object就像写给WeatherViewModel的说明书：请先去仓库（Application）里拿一节电池（Repository），装进车里（传进构造函数），再把这辆车吐出来！
    companion object {
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            //初始化器
            initializer {
                //1.先去找到Application单例
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                //2.打开单例中的container把里面的Repository取出来
                val weatherRepository = application.container.weatherRepository
                //3.把Repository传给ViewModel
                WeatherViewModel(weatherRepository = weatherRepository)
            }
        }
    }
}
