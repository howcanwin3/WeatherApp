package com.example.weatherforecastapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherforecastapp.MyApplication // 关键导入
import com.example.weatherforecastapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    
    // 1. 内部修改用的状态
    private val _uiState = MutableStateFlow(
        WeatherUiState("加载中...", "--℃", "正在获取数据", emptyList())
    )
    // 2. 外部只读的状态
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // ViewModel 一创建就自动获取天气
        fetchWeather("101020100")
    }

    private fun fetchWeather(locationId: String) {
        viewModelScope.launch {
            try {
                // 3. 调用 Repository 拿数据
                _uiState.value = weatherRepository.getWeather(locationId)
            } catch (e: Exception) {
                e.printStackTrace()
                // 出错时也可以更新 UI 提示
                _uiState.value = _uiState.value.copy(weatherDescription = "加载失败")
            }
        }
    }

    companion object {
        // 这个 Factory 就是告诉系统：创建 WeatherViewModel 的时候，去 MyApplication 里拿 Repository
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // 1. 获取全局 Application 实例
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                // 2. 从管家的容器里拿仓库
                val repository = application.container.weatherRepository
                // 3. 构造出 ViewModel（注意参数名要对上）
                WeatherViewModel(weatherRepository = repository)
            }
        }
    }
}
