package com.example.weatherforecastapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherforecastapp.data.local.WeatherDatabase
import com.example.weatherforecastapp.data.remote.WeatherApi
import com.example.weatherforecastapp.data.repository.WeatherRepository
import com.example.weatherforecastapp.data.repository.WeatherRepositoryImp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(locationId: String, apiKey: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val resultState = weatherRepository.getWeather(locationId, apiKey)
                _uiState.value = resultState
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // 直接从系统环境拿到 Context，不需要自定义 Application 类
                val application = this[APPLICATION_KEY]!!
                
                // 1. 初始化数据库
                val database = WeatherDatabase.getDatabase(application)
                
                // 2. 初始化仓库，并手动把“零件”（ApiService 和 Dao）装进去
                val repository = WeatherRepositoryImp(
                    apiService = WeatherApi.retrofitService,
                    weatherDao = database.weatherDao()
                )
                
                // 3. 返回创建好的 ViewModel
                WeatherViewModel(weatherRepository = repository)
            }
        }
    }
}
