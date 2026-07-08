package com.example.weatherforecastapp.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherforecastapp.WeatherApplication
import com.example.weatherforecastapp.data.config.WeatherDefaults
import com.example.weatherforecastapp.data.model.CitySearchCandidate
import com.example.weatherforecastapp.data.model.FavoriteCitySummary
import com.example.weatherforecastapp.data.model.toCandidate
import com.example.weatherforecastapp.data.repository.WeatherRepository
import com.example.weatherforecastapp.data.store.StartupSnapshotStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val startupSnapshotStore: StartupSnapshotStore,
) : ViewModel() {

    private sealed interface FetchTarget {
        data class CityQuery(val query: String) : FetchTarget
        data class CitySelection(val candidate: CitySearchCandidate) : FetchTarget
        data class Coordinates(val latitude: Double, val longitude: Double) : FetchTarget
    }

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow(WeatherPage.Home)
    val currentPage: StateFlow<WeatherPage> = _currentPage.asStateFlow()

    private val _currentLocationWeather = MutableStateFlow<WeatherUiState.Success?>(null)
    val currentLocationWeather: StateFlow<WeatherUiState.Success?> = _currentLocationWeather.asStateFlow()

    private val _cityManagerState = MutableStateFlow(CityManagerUiState())
    val cityManagerState: StateFlow<CityManagerUiState> = _cityManagerState.asStateFlow()

    private val _homeCityCards = MutableStateFlow<List<HomeCityCard>>(emptyList())
    val homeCityCards: StateFlow<List<HomeCityCard>> = _homeCityCards.asStateFlow()

    private val _selectedHomeCardKey = MutableStateFlow<String?>(null)
    val selectedHomeCardKey: StateFlow<String?> = _selectedHomeCardKey.asStateFlow()

    private var activeFetchTarget: FetchTarget = FetchTarget.CityQuery(WeatherDefaults.DEFAULT_CITY_QUERY)
    private var hasRestoredStartupSnapshot = false

    init {
        viewModelScope.launch {
            weatherRepository.observeFavoriteCitySummaries().collectLatest { favorites ->
                _cityManagerState.update { state ->
                    state.copy(favoriteCities = favorites)
                }
                refreshHomeCityCards(favorites = favorites)
            }
        }
    }

    fun restoreStartupSnapshot() {
        if (hasRestoredStartupSnapshot) return
        hasRestoredStartupSnapshot = true

        viewModelScope.launch {
            val snapshot = startupSnapshotStore.readSnapshot()
            val defaultFavorite = weatherRepository.getDefaultFavoriteCity()
            val cachedState = snapshot?.weatherQuery?.let {
                weatherRepository.getCachedWeatherByQuery(it, sourceLabel = "离线缓存 · 启动秒开")
            } ?: defaultFavorite?.displayName?.let {
                weatherRepository.getCachedWeatherByQuery(it, sourceLabel = "离线缓存 · 启动秒开")
            } ?: weatherRepository.getCachedWeatherByQuery(
                query = WeatherDefaults.DEFAULT_CITY_QUERY,
                sourceLabel = "离线缓存 · 启动秒开"
            )

            if (cachedState != null) {
                if (snapshot?.selectedCardKey == StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY) {
                    _currentLocationWeather.value = cachedState
                }
                _selectedHomeCardKey.value = snapshot?.selectedCardKey ?: defaultFavorite?.cityId
                _uiState.value = cachedState
                refreshHomeCityCards(currentLocation = _currentLocationWeather.value)
                activeFetchTarget = when (snapshot?.selectedCardKey) {
                    StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY -> activeFetchTarget
                    else -> FetchTarget.CityQuery(snapshot?.weatherQuery ?: cachedState.cityName)
                }
            }
        }
    }

    fun openCityManager() {
        _currentPage.value = WeatherPage.CityManager
    }

    fun returnHome() {
        _currentPage.value = WeatherPage.Home
    }

    fun updateCityManagerSearchQuery(query: String) {
        _cityManagerState.update { state ->
            state.copy(
                searchQuery = query,
                searchResults = emptyList(),
                selectedCandidate = null,
                previewWeather = null,
                previewErrorMessage = null,
                isSearching = false,
                isPreviewLoading = false
            )
        }
    }

    fun loadPreferredHomeWeather(showLoading: Boolean = true) {
        viewModelScope.launch {
            val defaultFavorite = weatherRepository.getDefaultFavoriteCity()
            if (defaultFavorite != null) {
                loadHomeWeather(defaultFavorite.toCandidate(), showLoading = showLoading)
            } else {
                loadDefaultCityWeather(showLoading = showLoading)
            }
        }
    }

    fun loadDefaultCityWeather(
        query: String = WeatherDefaults.DEFAULT_CITY_QUERY,
        showLoading: Boolean = true,
    ) {
        val normalizedQuery = query.trim().ifBlank { WeatherDefaults.DEFAULT_CITY_QUERY }
        activeFetchTarget = FetchTarget.CityQuery(normalizedQuery)

        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = WeatherUiState.Loading
            }
            try {
                val candidate = resolveCandidateForHome(normalizedQuery)
                if (candidate == null) {
                    _uiState.value = WeatherUiState.Error(
                        message = "未找到城市：$normalizedQuery"
                    )
                } else {
                    loadHomeWeather(candidate, showLoading = showLoading)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun refreshVisibleHomeWeather(showLoading: Boolean = true) {
        val selectedKey = _selectedHomeCardKey.value
        if (selectedKey == StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY) {
            return
        }

        val selectedFavorite = _cityManagerState.value.favoriteCities.firstOrNull { it.cityId == selectedKey }
        if (selectedFavorite != null) {
            viewModelScope.launch {
                loadHomeWeather(selectedFavorite.toCandidate(), showLoading = showLoading)
            }
            return
        }

        when (val target = activeFetchTarget) {
            is FetchTarget.CitySelection -> {
                viewModelScope.launch {
                    loadHomeWeather(target.candidate, showLoading = showLoading)
                }
            }
            is FetchTarget.CityQuery -> loadDefaultCityWeather(target.query, showLoading = showLoading)
            is FetchTarget.Coordinates -> Unit
        }
    }

    fun searchCitiesForManager() {
        val query = _cityManagerState.value.searchQuery.trim()
        if (query.isBlank()) {
            _cityManagerState.update { state ->
                state.copy(
                    searchResults = emptyList(),
                    selectedCandidate = null,
                    previewWeather = null,
                    previewErrorMessage = "请先输入要搜索的城市"
                )
            }
            return
        }

        viewModelScope.launch {
            _cityManagerState.update { state ->
                state.copy(
                    isSearching = true,
                    selectedCandidate = null,
                    previewWeather = null,
                    previewErrorMessage = null
                )
            }

            try {
                val candidates = weatherRepository.searchCities(query)
                val preferredCandidate = findPreferredCandidate(query, candidates)
                _cityManagerState.update { state ->
                    state.copy(
                        searchResults = candidates,
                        isSearching = false,
                        previewErrorMessage = if (candidates.isEmpty()) {
                            "未找到城市：$query"
                        } else {
                            null
                        }
                    )
                }
                if (preferredCandidate != null) {
                    previewSearchCandidate(preferredCandidate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _cityManagerState.update { state ->
                    state.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        previewErrorMessage = e.localizedMessage ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun previewSearchCandidate(candidate: CitySearchCandidate) {
        viewModelScope.launch {
            _cityManagerState.update { state ->
                state.copy(
                    selectedCandidate = candidate,
                    previewWeather = null,
                    previewErrorMessage = null,
                    isPreviewLoading = true
                )
            }

            when (val result = weatherRepository.getWeatherByCity(candidate)) {
                is WeatherUiState.Success -> {
                    _cityManagerState.update { state ->
                        state.copy(
                            selectedCandidate = candidate,
                            previewWeather = result,
                            previewErrorMessage = null,
                            isPreviewLoading = false
                        )
                    }
                }

                is WeatherUiState.Error -> {
                    _cityManagerState.update { state ->
                        state.copy(
                            selectedCandidate = candidate,
                            previewWeather = null,
                            previewErrorMessage = result.message,
                            isPreviewLoading = false
                        )
                    }
                }

                WeatherUiState.Loading -> Unit
            }
        }
    }

    fun addSelectedCityToFavorites() {
        val candidate = _cityManagerState.value.selectedCandidate ?: return
        viewModelScope.launch {
            weatherRepository.addFavoriteCity(candidate)
        }
    }

    fun removeFavoriteCity(summary: FavoriteCitySummary) {
        val wasSelectedHomeCard = _selectedHomeCardKey.value == summary.cityId
        viewModelScope.launch {
            weatherRepository.removeFavoriteCity(summary.cityId)
            _cityManagerState.update { state ->
                if (state.selectedCandidate?.id == summary.cityId) {
                    state.copy(
                        selectedCandidate = null,
                        previewWeather = null,
                        previewErrorMessage = null,
                        isPreviewLoading = false
                    )
                } else {
                    state
                }
            }

            if (wasSelectedHomeCard) {
                val currentLocation = _currentLocationWeather.value
                val defaultFavorite = weatherRepository.getDefaultFavoriteCity()
                when {
                    currentLocation != null -> showCurrentLocationWeather()
                    defaultFavorite != null -> loadHomeWeather(defaultFavorite.toCandidate(), showLoading = false)
                    else -> loadDefaultCityWeather(showLoading = true)
                }
            }
        }
    }

    fun setDefaultFavoriteCity(summary: FavoriteCitySummary) {
        viewModelScope.launch {
            weatherRepository.setDefaultFavoriteCity(summary.cityId)
        }
    }

    fun selectFavoriteCity(summary: FavoriteCitySummary) {
        _currentPage.value = WeatherPage.Home
        viewModelScope.launch {
            loadHomeWeather(summary.toCandidate())
        }
    }

    fun selectHomeCard(card: HomeCityCard) {
        if (card.isCurrentLocation) {
            showCurrentLocationWeather()
            return
        }

        val summary = _cityManagerState.value.favoriteCities.firstOrNull { it.cityId == card.key }
        if (summary != null) {
            viewModelScope.launch {
                loadHomeWeather(summary.toCandidate(), showLoading = false)
            }
            return
        }

        loadDefaultCityWeather(card.cityName, showLoading = false)
    }

    fun showCurrentLocationWeather() {
        val currentLocation = _currentLocationWeather.value ?: return
        _selectedHomeCardKey.value = StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY
        startupSnapshotStore.saveCurrentLocationSnapshot(currentLocation.cityName)
        _currentPage.value = WeatherPage.Home
        _uiState.value = currentLocation
    }

    fun loadWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        showLoading: Boolean = true,
    ) {
        activeFetchTarget = FetchTarget.Coordinates(latitude, longitude)

        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = WeatherUiState.Loading
            }
            try {
                when (val resultState = weatherRepository.getWeatherByCoordinates(latitude, longitude)) {
                    is WeatherUiState.Success -> {
                        _currentLocationWeather.value = resultState
                        _selectedHomeCardKey.value = StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY
                        startupSnapshotStore.saveCurrentLocationSnapshot(resultState.cityName)
                        _uiState.value = resultState
                        refreshHomeCityCards(currentLocation = resultState)
                    }

                    is WeatherUiState.Error -> {
                        _uiState.value = resultState
                    }

                    WeatherUiState.Loading -> Unit
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WeatherUiState.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun refreshWeather() {
        when (val target = activeFetchTarget) {
            is FetchTarget.CityQuery -> loadDefaultCityWeather(target.query)
            is FetchTarget.CitySelection -> {
                viewModelScope.launch {
                    loadHomeWeather(target.candidate)
                }
            }
            is FetchTarget.Coordinates -> Unit
        }
    }

    private suspend fun loadHomeWeather(
        candidate: CitySearchCandidate,
        showLoading: Boolean = true,
    ) {
        activeFetchTarget = FetchTarget.CitySelection(candidate)
        _selectedHomeCardKey.value = candidate.id
        if (showLoading) {
            _uiState.value = WeatherUiState.Loading
        }
        when (val resultState = weatherRepository.getWeatherByCity(candidate)) {
            is WeatherUiState.Success -> {
                startupSnapshotStore.saveCitySnapshot(candidate.id, candidate.displayName)
                _uiState.value = resultState
            }

            is WeatherUiState.Error -> {
                _uiState.value = resultState
            }

            WeatherUiState.Loading -> Unit
        }
    }

    private fun refreshHomeCityCards(
        favorites: List<FavoriteCitySummary> = _cityManagerState.value.favoriteCities,
        currentLocation: WeatherUiState.Success? = _currentLocationWeather.value,
    ) {
        val cards = buildList {
            currentLocation?.let { add(it.toHomeCityCard()) }
            favorites.forEach { add(it.toHomeCityCard()) }
        }
        _homeCityCards.value = cards

        val selectedKey = _selectedHomeCardKey.value
        if (selectedKey == null || cards.none { it.key == selectedKey }) {
            _selectedHomeCardKey.value = cards.firstOrNull()?.key
        }
    }

    private suspend fun resolveCandidateForHome(query: String): CitySearchCandidate? {
        val candidates = weatherRepository.searchCities(query)
        return findPreferredCandidate(query, candidates) ?: candidates.firstOrNull()
    }

    private fun findPreferredCandidate(
        query: String,
        candidates: List<CitySearchCandidate>
    ): CitySearchCandidate? {
        if (candidates.isEmpty()) {
            return null
        }

        val normalizedQuery = normalizeKeyword(query)
        val exactMatches = candidates.filter { normalizeKeyword(it.cityName) == normalizedQuery }
        if (exactMatches.size == 1) {
            return exactMatches.first()
        }

        val displayMatches = candidates.filter {
            normalizeKeyword(it.displayName.substringBefore("·").trim()) == normalizedQuery
        }
        return displayMatches.singleOrNull()
    }

    private fun normalizeKeyword(value: String): String {
        var normalized = value.substringBefore("·").trim()
        val suffixes = listOf("特别行政区", "自治州", "地区", "省", "市", "区", "县")
        var changed = true
        while (changed) {
            changed = false
            val matchedSuffix = suffixes.firstOrNull {
                normalized.endsWith(it) && normalized.length > it.length
            }
            if (matchedSuffix != null) {
                normalized = normalized.removeSuffix(matchedSuffix).trim()
                changed = true
            }
        }
        return normalized
    }

    private fun FavoriteCitySummary.toHomeCityCard(): HomeCityCard {
        return HomeCityCard(
            key = cityId,
            cityName = cityName,
            badge = if (isDefault) "默认城市" else regionText,
            description = description ?: "天气待同步",
            currentTemperature = currentTemperature ?: "--",
            highTemperature = highTemperature ?: "--",
            lowTemperature = lowTemperature ?: "--",
            isCurrentLocation = false,
            isDefault = isDefault,
        )
    }

    private fun WeatherUiState.Success.toHomeCityCard(): HomeCityCard {
        return HomeCityCard(
            key = StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY,
            cityName = cityName,
            badge = "当前位置",
            description = weatherDescription,
            currentTemperature = currentTemperature,
            highTemperature = highTemperature,
            lowTemperature = lowTemperature,
            isCurrentLocation = true,
            isDefault = false,
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as WeatherApplication
                WeatherViewModel(
                    weatherRepository = application.container.weatherRepository,
                    startupSnapshotStore = application.container.startupSnapshotStore,
                )
            }
        }
    }
}

enum class WeatherPage {
    Home,
    CityManager,
}

data class CityManagerUiState(
    val searchQuery: String = "",
    val searchResults: List<CitySearchCandidate> = emptyList(),
    val selectedCandidate: CitySearchCandidate? = null,
    val previewWeather: WeatherUiState.Success? = null,
    val previewErrorMessage: String? = null,
    val favoriteCities: List<FavoriteCitySummary> = emptyList(),
    val isSearching: Boolean = false,
    val isPreviewLoading: Boolean = false,
)

data class HomeCityCard(
    val key: String,
    val cityName: String,
    val badge: String,
    val description: String,
    val currentTemperature: String,
    val highTemperature: String,
    val lowTemperature: String,
    val isCurrentLocation: Boolean,
    val isDefault: Boolean,
)
