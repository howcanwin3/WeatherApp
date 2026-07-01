package com.example.weatherforecastapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.ui.theme.WeatherForecastAppTheme

@Composable
fun WeatherRoute(
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)
) {
    val uiState by weatherViewModel.uiState.collectAsState()
    val searchQuery by weatherViewModel.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        weatherViewModel.refreshWeather()
    }

    WeatherScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = weatherViewModel::updateSearchQuery,
        onSearch = { weatherViewModel.searchWeather() },
        onRetry = weatherViewModel::refreshWeather,
        modifier = modifier
    )
}

@Composable
fun WeatherScreenContent(
    uiState: WeatherUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.weather_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA0F1E3A),
                            Color(0x661B4D8C),
                            Color(0xCC07111F)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = { WeatherTopBar(onRefresh = onRetry) }
        ) { innerPadding ->
            when (uiState) {
                WeatherUiState.Loading -> LoadingScreen(
                    modifier = Modifier.padding(innerPadding)
                )

                is WeatherUiState.Success -> WeatherSuccessContent(
                    state = uiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    modifier = Modifier.padding(innerPadding)
                )

                is WeatherUiState.Error -> ErrorScreen(
                    message = uiState.message,
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onRetry = onRetry,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherTopBar(
    onRefresh: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "今日天气",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新天气",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.18f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在同步最新天气",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun WeatherSuccessContent(
    state: WeatherUiState.Success,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchCard(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch
        )
        HeroCard(state = state)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                title = "数据来源",
                value = state.sourceLabel,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "最近更新",
                value = state.lastUpdatedText,
                modifier = Modifier.weight(1f)
            )
        }

        ForecastCard(items = state.forecastItems)
    }
}

@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "城市搜索",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "支持模糊搜索，例如 上海、北京、深圳。",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("输入城市") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearch()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.55f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.72f),
                        cursorColor = Color.White
                    )
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSearch()
                    }
                ) {
                    Text("搜索")
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    state: WeatherUiState.Success
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = state.cityName,
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.currentTemperature,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.weatherDescription,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Text(
                    text = state.lastUpdatedText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ForecastCard(
    items: List<ForecastItem>
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "未来预报",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (items.isEmpty()) {
                Text(
                    text = "缓存数据暂未包含未来预报，网络恢复后会自动补全。",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                items.forEachIndexed { index, item ->
                    ForecastRow(item = item)
                    if (index != items.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.18f))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastRow(
    item: ForecastItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.dayOfWeek,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = item.weather,
                color = Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = item.temperatureRange,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorScreen(
    message: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color.White.copy(alpha = 0.18f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchCard(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearch
                )
                Spacer(modifier = Modifier.height(20.dp))
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "错误提示",
                    tint = Color(0xFFFFD166),
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "天气暂时加载失败",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(onClick = onRetry) {
                    Text(text = "重新加载")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherForecastAppTheme(dynamicColor = false) {
        WeatherScreenContent(
            uiState = WeatherUiState.Success(
                cityName = "上海",
                currentTemperature = "31℃",
                weatherDescription = "晴转多云",
                forecastItems = listOf(
                    ForecastItem("周三", "晴", "27℃ - 33℃"),
                    ForecastItem("周四", "多云", "26℃ - 31℃"),
                    ForecastItem("周五", "小雨", "24℃ - 29℃")
                ),
                lastUpdatedText = "更新于 07月01日 14:30",
                sourceLabel = "实时数据"
            ),
            searchQuery = "上海",
            onSearchQueryChange = {},
            onSearch = {},
            onRetry = {}
        )
    }
}
