package com.example.weatherforecastapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherforecastapp.WeatherApplication
import com.example.weatherforecastapp.data.config.WeatherDefaults
import com.example.weatherforecastapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow(WeatherDefaults.DEFAULT_CITY_QUERY)
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchWeather(query: String = _searchQuery.value) {
        val normalizedQuery = query.trim().ifBlank { WeatherDefaults.DEFAULT_CITY_QUERY }
        _searchQuery.value = normalizedQuery

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val resultState = weatherRepository.getWeather(normalizedQuery)
                if (resultState is WeatherUiState.Success) {
                    _searchQuery.value = resultState.cityName
                }
                _uiState.value = resultState
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun refreshWeather() {
        searchWeather(_searchQuery.value)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as WeatherApplication
                WeatherViewModel(weatherRepository = application.container.weatherRepository)
            }
        }
    }
}
