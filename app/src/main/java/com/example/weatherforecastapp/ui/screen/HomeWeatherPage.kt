package com.example.weatherforecastapp.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecastapp.R
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeWeatherScreen(
    uiState: WeatherUiState,
    homeCards: List<HomeCityCard>,
    selectedCardKey: String?,
    onSelectHomeCard: (HomeCityCard) -> Unit,
    onOpenCityManager: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    val selectedCard = homeCards.firstOrNull { it.key == selectedCardKey }
    val backgroundDescription = (uiState as? WeatherUiState.Success)?.weatherDescription
        ?: selectedCard?.description
        ?: ""

    WeatherSceneBackground(description = backgroundDescription) {
        when (uiState) {
            WeatherUiState.Loading -> LoadingHomeScreen()
            is WeatherUiState.Error -> ErrorHomeScreen(message = uiState.message, onRetry = onRetry)
            is WeatherUiState.Success -> HomeSuccessScreen(
                state = uiState,
                homeCards = homeCards,
                selectedCardKey = selectedCardKey,
                onSelectHomeCard = onSelectHomeCard,
                onOpenCityManager = onOpenCityManager,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun LoadingHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.weather_loading_text),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorHomeScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "天气加载失败", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.84f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(text = androidx.compose.ui.res.stringResource(R.string.weather_retry_button))
        }
    }
}

@Composable
private fun HomeSuccessScreen(
    state: WeatherUiState.Success,
    homeCards: List<HomeCityCard>,
    selectedCardKey: String?,
    onSelectHomeCard: (HomeCityCard) -> Unit,
    onOpenCityManager: () -> Unit,
    onRefresh: () -> Unit,
) {
    val forecastItems = state.forecastItems.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        HomeActionBar(onOpenCityManager = onOpenCityManager, onRefresh = onRefresh)
        Spacer(modifier = Modifier.height(14.dp))

        if (homeCards.isNotEmpty()) {
            HomeCityPager(
                cards = homeCards,
                selectedCardKey = selectedCardKey,
                onSelectHomeCard = onSelectHomeCard,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = state.cityName,
            color = Color.White.copy(alpha = 0.96f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = state.currentTemperature,
            color = Color(0xFFF0FBFF),
            fontSize = 78.sp,
            lineHeight = 78.sp,
            fontWeight = FontWeight.ExtraLight,
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${state.weatherDescription}  最高${state.highTemperature}  最低${state.lowTemperature}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherInfoChip(label = state.sourceLabel)
            WeatherInfoChip(label = state.lastUpdatedText)
        }

        Spacer(modifier = Modifier.weight(1f))

        ForecastPanel(items = forecastItems)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeCityPager(
    cards: List<HomeCityCard>,
    selectedCardKey: String?,
    onSelectHomeCard: (HomeCityCard) -> Unit,
) {
    val initialPage = cards.indexOfFirst { it.key == selectedCardKey }.takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(initialPage = initialPage) { cards.size }

    LaunchedEffect(cards, selectedCardKey) {
        val targetPage = cards.indexOfFirst { it.key == selectedCardKey }
        if (targetPage >= 0 && targetPage != pagerState.currentPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    LaunchedEffect(pagerState, cards, selectedCardKey) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val card = cards.getOrNull(page) ?: return@collect
                if (card.key != selectedCardKey) {
                    onSelectHomeCard(card)
                }
            }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = 10.dp,
        ) { page ->
            val card = cards[page]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSelectHomeCard(card) },
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.cityName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${weatherEmoji(card.description)} ${card.description}",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = card.currentTemperature,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                        )
                        Text(
                            text = "${card.highTemperature}/${card.lowTemperature}",
                            color = Color.White.copy(alpha = 0.74f),
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            cards.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(width = if (index == pagerState.currentPage) 18.dp else 6.dp, height = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index == pagerState.currentPage) Color.White
                            else Color.White.copy(alpha = 0.32f)
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeActionBar(
    onOpenCityManager: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.weather_top_bar_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassIconButton(symbol = "+", onClick = onOpenCityManager)
            GlassIconButton(symbol = "↻", onClick = onRefresh)
        }
    }
}

@Composable
private fun GlassIconButton(symbol: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun WeatherInfoChip(label: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.14f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 12.sp,
        )
    }
}

@Composable
private fun ForecastPanel(items: List<ForecastItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF5F7CAA).copy(alpha = 0.46f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            if (items.isEmpty()) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.weather_forecast_empty),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                val lows = items.mapNotNull { parseTemperatureValue(it.lowTemperature) }
                val highs = items.mapNotNull { parseTemperatureValue(it.highTemperature) }
                val minTemp = lows.minOrNull() ?: 0
                val maxTemp = highs.maxOrNull() ?: 0

                items.forEachIndexed { index, item ->
                    ForecastSummaryRow(
                        item = item,
                        minTemp = minTemp,
                        maxTemp = maxTemp,
                    )
                    if (index != items.lastIndex) {
                        Spacer(modifier = Modifier.height(11.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                        Spacer(modifier = Modifier.height(11.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = "查看近5日天气",
                        modifier = Modifier.padding(vertical = 15.dp),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastSummaryRow(item: ForecastItem, minTemp: Int, maxTemp: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.dayOfWeek,
            modifier = Modifier.width(42.dp),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.weather,
            modifier = Modifier.width(58.dp),
            color = Color.White.copy(alpha = 0.94f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = weatherEmoji(item.weather), fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.lowTemperature,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        TemperatureRangeBar(
            low = parseTemperatureValue(item.lowTemperature),
            high = parseTemperatureValue(item.highTemperature),
            globalMin = minTemp,
            globalMax = maxTemp,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.highTemperature,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TemperatureRangeBar(
    low: Int?,
    high: Int?,
    globalMin: Int,
    globalMax: Int,
    modifier: Modifier = Modifier,
) {
    val fullRange = (globalMax - globalMin).coerceAtLeast(1)
    val startFraction = ((low ?: globalMin) - globalMin).toFloat() / fullRange
    val endFraction = ((high ?: globalMax) - globalMin).toFloat() / fullRange

    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(endFraction.coerceIn(0f, 1f))
                .padding(start = 96.dp * startFraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFD45D), Color(0xFFFF8A5B))
                    )
                )
        )
    }
}
