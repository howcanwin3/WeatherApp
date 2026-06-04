package com.example.weatherforecastapp.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherViewModel : ViewModel() {//自定义的ViewModel首先都要继承官方的ViewModel
    //1.ViewModel定义一个内部私有的状态流，用于内部修改数据

    private val _uiState = MutableStateFlow(
        //给StateFlow一个初始值---数据流里可以是任何类型一个int 一个string 一个实例都行
        WeatherUiState("上海" ,
            currentTemperature = "17℃" ,
            weatherDescription = "多云 最高 24℃ 最低 16℃" ,
            forecastItems = listOf(ForecastItem("星期一", "晴", "17℃-----24℃"),
                ForecastItem("星期二", "多云", "16℃-----28℃"),
                ForecastItem("星期三", "雷阵雨", "20℃-----30℃"))
        )
    )
    //2.公开的只读state用于UI访问
    val uiState : StateFlow<WeatherUiState> = _uiState.asStateFlow()
}