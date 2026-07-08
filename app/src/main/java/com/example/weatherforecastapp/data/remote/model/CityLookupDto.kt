package com.example.weatherforecastapp.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityLookupDto(
    @SerialName("code") val code: String,
    @SerialName("location") val locations: List<CityLookupLocationDto> = emptyList(),
)

@Serializable
data class CityLookupLocationDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("adm1") val adm1: String = "",
    @SerialName("country") val country: String = "",
)
