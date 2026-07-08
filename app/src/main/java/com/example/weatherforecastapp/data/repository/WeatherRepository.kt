package com.example.weatherforecastapp.data.repository

import com.example.weatherforecastapp.data.config.WeatherDefaults
import com.example.weatherforecastapp.data.local.FavoriteCityEntity
import com.example.weatherforecastapp.data.local.WeatherDao
import com.example.weatherforecastapp.data.local.WeatherEntity
import com.example.weatherforecastapp.data.model.CitySearchCandidate
import com.example.weatherforecastapp.data.model.FavoriteCitySummary
import com.example.weatherforecastapp.data.remote.WeatherApiService
import com.example.weatherforecastapp.data.remote.model.CityLookupLocationDto
import com.example.weatherforecastapp.data.remote.model.WeatherDto
import com.example.weatherforecastapp.ui.mapper.toEntity
import com.example.weatherforecastapp.ui.mapper.toWeatherUiState
import com.example.weatherforecastapp.ui.mapper.toWeatherUiStateFromCache
import com.example.weatherforecastapp.ui.screen.WeatherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

interface WeatherRepository {
    fun observeFavoriteCitySummaries(): Flow<List<FavoriteCitySummary>>
    suspend fun searchCities(cityQuery: String): List<CitySearchCandidate>
    suspend fun getWeatherByCity(candidate: CitySearchCandidate): WeatherUiState
    suspend fun getWeatherByCoordinates(latitude: Double, longitude: Double): WeatherUiState
    suspend fun getCachedWeatherByQuery(query: String, sourceLabel: String = "离线缓存"): WeatherUiState.Success?
    suspend fun addFavoriteCity(candidate: CitySearchCandidate)
    suspend fun removeFavoriteCity(cityId: String)
    suspend fun setDefaultFavoriteCity(cityId: String)
    suspend fun getDefaultFavoriteCity(): FavoriteCitySummary?
    suspend fun isFavoriteCity(cityId: String): Boolean
}

class WeatherRepositoryImp(
    private val apiService: WeatherApiService,
    private val weatherDao: WeatherDao,
    private val apiKey: String
) : WeatherRepository {

    override fun observeFavoriteCitySummaries(): Flow<List<FavoriteCitySummary>> {
        return weatherDao.observeFavoriteCitySummaries()
    }

    override suspend fun searchCities(cityQuery: String): List<CitySearchCandidate> =
        withContext(Dispatchers.IO) {
            ensureApiKey()

            val normalizedQuery = cityQuery.trim().ifBlank { WeatherDefaults.DEFAULT_CITY_QUERY }
            val cityLookup = apiService.lookupCity(
                location = normalizedQuery,
                apiKey = apiKey,
                number = 10
            )

            if (cityLookup.code != "200") {
                return@withContext emptyList()
            }

            cityLookup.locations
                .map { it.toSearchCandidate() }
                .distinctBy(CitySearchCandidate::id)
        }

    override suspend fun getWeatherByCity(candidate: CitySearchCandidate): WeatherUiState =
        withContext(Dispatchers.IO) {
            apiKeyError()?.let { return@withContext it }

            try {
                val response: WeatherDto = apiService.get3dWeather(candidate.id, apiKey)
                if (response.code != "200") {
                    return@withContext cachedOrError(
                        query = candidate.displayName,
                        defaultMessage = "天气服务暂时不可用：${candidate.displayName}"
                    )
                }

                weatherDao.insertWeather(response.toEntity(candidate.displayName))
                response.toWeatherUiState(candidate.displayName)
            } catch (e: Exception) {
                cachedOrError(
                    query = candidate.displayName,
                    defaultMessage = "未能加载 ${candidate.displayName} 的天气：${e.localizedMessage ?: "未知错误"}"
                )
            }
        }

    override suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): WeatherUiState = withContext(Dispatchers.IO) {
        apiKeyError()?.let { return@withContext it }

        val coordinateQuery = formatCoordinateQuery(latitude, longitude)
        var resolvedCityName: String? = null

        try {
            val cityLookup = apiService.lookupCity(
                location = coordinateQuery,
                apiKey = apiKey
            )

            if (cityLookup.code != "200") {
                return@withContext cachedOrError(
                    query = WeatherDefaults.DEFAULT_CITY_QUERY,
                    defaultMessage = "当前位置解析失败，请稍后重试"
                )
            }

            val city = cityLookup.locations.firstOrNull()?.toSearchCandidate()
                ?: return@withContext cachedOrError(
                    query = WeatherDefaults.DEFAULT_CITY_QUERY,
                    defaultMessage = "未找到当前位置对应的城市"
                )

            resolvedCityName = city.displayName

            val response = apiService.get3dWeather(coordinateQuery, apiKey)
            if (response.code != "200") {
                return@withContext cachedOrError(
                    query = resolvedCityName,
                    defaultMessage = "当前位置天气加载失败，请稍后重试"
                )
            }

            weatherDao.insertWeather(response.toEntity(resolvedCityName))
            response.toWeatherUiState(
                cityName = resolvedCityName,
                sourceLabel = "实时数据 · 当前定位"
            )
        } catch (e: Exception) {
            cachedOrError(
                query = resolvedCityName ?: WeatherDefaults.DEFAULT_CITY_QUERY,
                defaultMessage = "当前位置天气加载失败：${e.localizedMessage ?: "未知错误"}"
            )
        }
    }

    override suspend fun getCachedWeatherByQuery(
        query: String,
        sourceLabel: String
    ): WeatherUiState.Success? = withContext(Dispatchers.IO) {
        findCachedWeather(query)?.toWeatherUiStateFromCache(sourceLabel)
    }

    override suspend fun addFavoriteCity(candidate: CitySearchCandidate) {
        withContext(Dispatchers.IO) {
            weatherDao.insertFavoriteCity(
                FavoriteCityEntity(
                    cityId = candidate.id,
                    cityName = candidate.cityName,
                    regionText = candidate.regionText,
                    displayName = candidate.displayName
                )
            )
            if (weatherDao.getDefaultFavoriteCityId() == null) {
                weatherDao.replaceDefaultFavoriteCity(candidate.id)
            }
        }
    }

    override suspend fun removeFavoriteCity(cityId: String) {
        withContext(Dispatchers.IO) {
            val removedDefaultCityId = weatherDao.getDefaultFavoriteCityId()
            weatherDao.deleteFavoriteCity(cityId)
            if (removedDefaultCityId == cityId) {
                weatherDao.getTopFavoriteCityId()?.let(weatherDao::replaceDefaultFavoriteCity)
            }
        }
    }

    override suspend fun setDefaultFavoriteCity(cityId: String) {
        withContext(Dispatchers.IO) {
            weatherDao.replaceDefaultFavoriteCity(cityId)
        }
    }

    override suspend fun getDefaultFavoriteCity(): FavoriteCitySummary? = withContext(Dispatchers.IO) {
        weatherDao.getDefaultFavoriteCitySummary()
    }

    override suspend fun isFavoriteCity(cityId: String): Boolean = withContext(Dispatchers.IO) {
        weatherDao.isFavoriteCity(cityId)
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

    private fun formatCoordinateQuery(latitude: Double, longitude: Double): String {
        return String.format(Locale.US, "%.2f,%.2f", longitude, latitude)
    }

    private fun apiKeyError(): WeatherUiState.Error? {
        return if (apiKey.isBlank()) {
            WeatherUiState.Error(
                message = "未配置 API Key，请在 local.properties 中设置 API_KEY"
            )
        } else {
            null
        }
    }

    private fun ensureApiKey() {
        check(apiKey.isNotBlank()) { "未配置 API Key，请在 local.properties 中设置 API_KEY" }
    }

    private fun CityLookupLocationDto.toSearchCandidate(): CitySearchCandidate {
        val regionParts = buildList {
            if (adm1.isNotBlank() && adm1 != name) {
                add(adm1)
            }
            if (country.isNotBlank()) {
                add(country)
            }
        }.distinct()

        val regionText = regionParts.joinToString(" · ")
        val displayName = if (regionText.isBlank()) {
            name
        } else {
            "$name · $regionText"
        }

        return CitySearchCandidate(
            id = id,
            cityName = name,
            regionText = regionText,
            displayName = displayName
        )
    }
}
