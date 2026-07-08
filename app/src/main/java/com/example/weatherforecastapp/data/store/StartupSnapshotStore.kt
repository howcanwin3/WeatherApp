package com.example.weatherforecastapp.data.store

import android.content.Context

data class StartupSnapshot(
    val selectedCardKey: String,
    val weatherQuery: String,
)

class StartupSnapshotStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCitySnapshot(cardKey: String, weatherQuery: String) {
        saveSnapshot(cardKey = cardKey, weatherQuery = weatherQuery)
    }

    fun saveCurrentLocationSnapshot(weatherQuery: String) {
        saveSnapshot(cardKey = CURRENT_LOCATION_CARD_KEY, weatherQuery = weatherQuery)
    }

    fun readSnapshot(): StartupSnapshot? {
        val selectedCardKey = preferences.getString(KEY_SELECTED_CARD, null)
        val weatherQuery = preferences.getString(KEY_WEATHER_QUERY, null)
        if (selectedCardKey.isNullOrBlank() || weatherQuery.isNullOrBlank()) {
            return null
        }
        return StartupSnapshot(
            selectedCardKey = selectedCardKey,
            weatherQuery = weatherQuery,
        )
    }

    private fun saveSnapshot(cardKey: String, weatherQuery: String) {
        preferences.edit()
            .putString(KEY_SELECTED_CARD, cardKey)
            .putString(KEY_WEATHER_QUERY, weatherQuery)
            .apply()
    }

    companion object {
        const val CURRENT_LOCATION_CARD_KEY = "__current_location__"

        private const val PREFS_NAME = "weather_startup_snapshot"
        private const val KEY_SELECTED_CARD = "selected_card_key"
        private const val KEY_WEATHER_QUERY = "weather_query"
    }
}
