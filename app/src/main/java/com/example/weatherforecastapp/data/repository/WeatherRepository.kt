package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.config.WeatherDefaults
import com.example.weatherforecastapp.data.local.WeatherDao
import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.mapper.toEntity
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.mapper.toWeatherUiStateFromCache
import com.example.weatherforecastapp.ui.screen.WeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WeatherRepository {
    suspend fun getWeather(cityQuery: String): WeatherUiState
}

class WeatherRepositoryImp(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val apiKey: String
) : WeatherRepository {

    override suspend fun getWeather(cityQuery: String): WeatherUiState =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext WeatherUiState.Error(
                    message = "未配置 API Key，请在 local.properties 中设置 API_KEY"
                )
            }

            val normalizedQuery = cityQuery.trim().ifBlank { WeatherDefaults.DEFAULT_CITY_QUERY }
            var resolvedCityName: String? = null

            try {
                val cityLookup = apiService.lookupCity(
                    location = normalizedQuery,
                    apiKey = apiKey
                )

                if (cityLookup.code != "200") {
                    return@withContext cachedOrError(
                        query = normalizedQuery,
                        defaultMessage = "未找到城市：$normalizedQuery"
                    )
                }

                val city = cityLookup.locations.firstOrNull()
                    ?: return@withContext cachedOrError(
                        query = normalizedQuery,
                        defaultMessage = "未找到城市：$normalizedQuery"
                    )

                resolvedCityName = city.name

                val response: WeatherDto = apiService.get3dWeather(city.id, apiKey)
                if (response.code != "200") {
                    return@withContext cachedOrError(
                        query = resolvedCityName,
                        defaultMessage = "天气服务暂时不可用：$normalizedQuery"
                    )
                }

                weatherDao.insertWeather(response.toEntity(resolvedCityName))
                response.toWeatherUiState(resolvedCityName)
            } catch (e: Exception) {
                cachedOrError(
                    query = resolvedCityName ?: normalizedQuery,
                    defaultMessage = "未能加载 $normalizedQuery 的天气：${e.localizedMessage ?: "未知错误"}"
                )
            }
        }

    private fun cachedOrError(
        query: String?,
        defaultMessage: String
    ): WeatherUiState {
        val cachedEntity = query?.let(::findCachedWeather)
        return cachedEntity?.toWeatherUiStateFromCache()
            ?: WeatherUiState.Error(message = defaultMessage)
    }

    private fun findCachedWeather(query: String): WeatherEntity? {
        return weatherDao.getWeatherByCity(query)
            ?: weatherDao.searchWeatherByKeyword(query)
            ?: if (query != WeatherDefaults.DEFAULT_CITY_QUERY) {
                weatherDao.getWeatherByCity(WeatherDefaults.DEFAULT_CITY_QUERY)
            } else {
                null
            }
    }
}
