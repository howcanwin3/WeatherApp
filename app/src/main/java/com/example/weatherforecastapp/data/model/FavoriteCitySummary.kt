package com.example.weatherforecastapp.data.model

data class FavoriteCitySummary(
    val cityId: String,
    val cityName: String,
    val regionText: String,
    val displayName: String,
    val currentTemperature: String?,
    val description: String?,
    val highTemperature: String?,
    val lowTemperature: String?,
    val lastUpdated: Long?,
    val isDefault: Boolean,
)

fun FavoriteCitySummary.toCandidate(): CitySearchCandidate {
    return CitySearchCandidate(
        id = cityId,
        cityName = cityName,
        regionText = regionText,
        displayName = displayName
    )
}
