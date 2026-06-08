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

class WeatherViewModel(private val weatherRepository : WeatherRepository ) : ViewModel() {//自定义的ViewModel首先都要继承官方的ViewModel
    //1.ViewModel定义一个内部私有的状态流，用于内部修改数据

    private val _uiState = MutableStateFlow(WeatherUiState("","", "", emptyList<ForecastItem>()))
        //给StateFlow一个初始值---数据流里可以是任何类型一个int 一个string 一个实例都行
    //2.公开的只读state用于UI访问
    val uiState : StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(locationId : String, apiKey : String) {
        viewModelScope.launch{
            try{
                val dto = weatherRepository.getWeather(locationId,apiKey)
                _uiState.value = dto.toWeatherUiState()
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }
    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                val weatherRepository = application.container.weatherRepository
                WeatherViewModel(weatherRepository = weatherRepository)
            }
        }
    }



}


