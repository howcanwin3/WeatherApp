package com.example.weatherforecastapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.data.model.CitySearchCandidate
import com.example.weatherforecastapp.data.model.FavoriteCitySummary

@Composable
fun CityManagerScreen(
    state: CityManagerUiState,
    currentLocationWeather: WeatherUiState.Success?,
    locationNotice: String?,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPreviewCandidate: (CitySearchCandidate) -> Unit,
    onAddSelectedCity: () -> Unit,
    onOpenFavoriteCity: (FavoriteCitySummary) -> Unit,
    onSetDefaultFavorite: (FavoriteCitySummary) -> Unit,
    onDeleteFavorite: (FavoriteCitySummary) -> Unit,
    onOpenCurrentLocation: () -> Unit,
    onRefreshLocation: () -> Unit,
) {
    val selectedId = state.selectedCandidate?.id
    val alreadyAdded = selectedId != null && state.favoriteCities.any { it.cityId == selectedId }

    ManagerSceneBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "← 返回",
                    modifier = Modifier.clickable { onBack() },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            item {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.weather_manage_cities_title),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.weather_manage_search_placeholder),
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 15.sp,
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.48f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                            focusedContainerColor = Color(0xFF171F2E).copy(alpha = 0.92f),
                            unfocusedContainerColor = Color(0xFF171F2E).copy(alpha = 0.92f),
                            focusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
                            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
                        ),
                    )
                    Button(
                        onClick = onSearch,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(text = androidx.compose.ui.res.stringResource(R.string.weather_search_button), fontSize = 14.sp)
                    }
                }
            }

            if (!locationNotice.isNullOrBlank()) {
                item {
                    EmptyHintCard(
                        title = androidx.compose.ui.res.stringResource(R.string.weather_location_notice_title),
                        message = locationNotice,
                        actionLabel = androidx.compose.ui.res.stringResource(R.string.weather_location_retry_button),
                        onAction = onRefreshLocation,
                    )
                }
            }

            if (state.isSearching) {
                item {
                    SearchPreviewLoadingCard(text = androidx.compose.ui.res.stringResource(R.string.weather_searching_candidates))
                }
            }

            if (state.searchResults.isNotEmpty()) {
                item { SectionTitle(title = androidx.compose.ui.res.stringResource(R.string.weather_search_results_title)) }
                items(state.searchResults, key = { it.id }) { candidate ->
                    SearchResultCard(
                        candidate = candidate,
                        isSelected = candidate.id == selectedId,
                        onClick = { onPreviewCandidate(candidate) },
                    )
                }
            }

            if (state.isPreviewLoading) {
                item { SectionTitle(title = androidx.compose.ui.res.stringResource(R.string.weather_search_preview_title)) }
                item { SearchPreviewLoadingCard(text = androidx.compose.ui.res.stringResource(R.string.weather_preview_loading_text)) }
            }

            state.previewWeather?.let { preview ->
                item { SectionTitle(title = androidx.compose.ui.res.stringResource(R.string.weather_search_preview_title)) }
                item {
                    SearchPreviewCard(
                        state = preview,
                        alreadyAdded = alreadyAdded,
                        onAdd = onAddSelectedCity,
                    )
                }
            }

            if (!state.previewErrorMessage.isNullOrBlank()) {
                item {
                    EmptyHintCard(
                        title = androidx.compose.ui.res.stringResource(R.string.weather_search_preview_title),
                        message = state.previewErrorMessage,
                    )
                }
            }

            item { SectionTitle(title = androidx.compose.ui.res.stringResource(R.string.weather_location_section_title)) }
            item {
                if (currentLocationWeather != null) {
                    WeatherSummaryCard(
                        title = currentLocationWeather.cityName,
                        badge = androidx.compose.ui.res.stringResource(R.string.weather_current_location_badge),
                        description = currentLocationWeather.weatherDescription,
                        currentTemperature = currentLocationWeather.currentTemperature,
                        highTemperature = currentLocationWeather.highTemperature,
                        lowTemperature = currentLocationWeather.lowTemperature,
                        onClick = onOpenCurrentLocation,
                    )
                } else {
                    EmptyHintCard(
                        title = androidx.compose.ui.res.stringResource(R.string.weather_location_empty_title),
                        message = androidx.compose.ui.res.stringResource(R.string.weather_location_empty_message),
                        actionLabel = androidx.compose.ui.res.stringResource(R.string.weather_location_retry_button),
                        onAction = onRefreshLocation,
                    )
                }
            }

            item { SectionTitle(title = androidx.compose.ui.res.stringResource(R.string.weather_saved_cities_title)) }
            if (state.favoriteCities.isEmpty()) {
                item {
                    EmptyHintCard(
                        title = androidx.compose.ui.res.stringResource(R.string.weather_favorites_empty_title),
                        message = androidx.compose.ui.res.stringResource(R.string.weather_favorites_empty_message),
                    )
                }
            } else {
                items(state.favoriteCities, key = { it.cityId }) { summary ->
                    WeatherSummaryCard(
                        title = summary.cityName,
                        badge = summary.regionText,
                        description = summary.description ?: androidx.compose.ui.res.stringResource(R.string.weather_placeholder_description),
                        currentTemperature = summary.currentTemperature ?: "--",
                        highTemperature = summary.highTemperature ?: "--",
                        lowTemperature = summary.lowTemperature ?: "--",
                        isDefault = summary.isDefault,
                        onSetDefault = if (summary.isDefault) null else ({ onSetDefaultFavorite(summary) }),
                        onDelete = { onDeleteFavorite(summary) },
                        onClick = { onOpenFavoriteCity(summary) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SearchResultCard(
    candidate: CitySearchCandidate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (isSelected) Color(0xFF203050).copy(alpha = 0.96f) else Color(0xFF131D2A).copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = candidate.cityName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = candidate.regionText,
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchPreviewLoadingCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF131D2A).copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color.White)
            Text(text = text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SearchPreviewCard(
    state: WeatherUiState.Success,
    alreadyAdded: Boolean,
    onAdd: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.verticalGradient(weatherCardGradient(state.weatherDescription)))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.cityName,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${weatherEmoji(state.weatherDescription)} ${state.weatherDescription}",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = state.currentTemperature,
                    color = Color(0xFFF0FAFF),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraLight,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "最高${state.highTemperature} / 最低${state.lowTemperature}",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAdd,
                enabled = !alreadyAdded,
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(
                    text = if (alreadyAdded) {
                        androidx.compose.ui.res.stringResource(R.string.weather_added_button)
                    } else {
                        androidx.compose.ui.res.stringResource(R.string.weather_add_favorite_button)
                    },
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyHintCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF131D2A).copy(alpha = 0.95f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
            if (!actionLabel.isNullOrBlank() && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) {
                    Text(text = actionLabel, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun WeatherSummaryCard(
    title: String,
    badge: String,
    description: String,
    currentTemperature: String,
    highTemperature: String,
    lowTemperature: String,
    isDefault: Boolean = false,
    onSetDefault: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(weatherCardGradient(description)))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MiniInfoChip(label = badge)
                        if (isDefault) {
                            MiniInfoChip(label = androidx.compose.ui.res.stringResource(R.string.weather_default_city_badge))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.94f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTemperature,
                        color = Color(0xFFF0FAFF),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraLight,
                    )
                    Text(
                        text = "$highTemperature / $lowTemperature",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                    )
                }
            }
            if (onSetDefault != null || onDelete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onSetDefault?.let {
                        ActionChip(
                            label = androidx.compose.ui.res.stringResource(R.string.weather_set_default_button),
                            onClick = it,
                        )
                    }
                    onDelete?.let {
                        ActionChip(
                            label = androidx.compose.ui.res.stringResource(R.string.weather_delete_favorite_button),
                            onClick = it,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniInfoChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            color = Color.White,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.15f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}


