package com.example.weatherforecastapp.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherforecastapp.R
import com.example.weatherforecastapp.data.store.StartupSnapshotStore
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun WeatherRoute(
    weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val uiState by weatherViewModel.uiState.collectAsState()
    val currentPage by weatherViewModel.currentPage.collectAsState()
    val cityManagerState by weatherViewModel.cityManagerState.collectAsState()
    val currentLocationWeather by weatherViewModel.currentLocationWeather.collectAsState()
    val homeCityCards by weatherViewModel.homeCityCards.collectAsState()
    val selectedHomeCardKey by weatherViewModel.selectedHomeCardKey.collectAsState()
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationFailedMessage = stringResource(R.string.weather_location_failed_message)
    val locationServiceDisabledMessage = stringResource(R.string.weather_location_service_disabled_message)
    val locationPermissionDeniedMessage = stringResource(R.string.weather_location_permission_denied_message)
    val locationSettingsMessage = stringResource(R.string.weather_location_settings_message)

    var hasRequestedLocationPermission by rememberSaveable { mutableStateOf(false) }
    var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
    var showPermissionSettingsGuide by rememberSaveable { mutableStateOf(false) }
    var showLocationServiceGuide by rememberSaveable { mutableStateOf(false) }
    var locationNotice by rememberSaveable { mutableStateOf<String?>(null) }

    fun fallbackToCityWeather(noticeMessage: String? = null) {
        locationNotice = noticeMessage
        weatherViewModel.loadPreferredHomeWeather(showLoading = uiState !is WeatherUiState.Success)
    }

    fun consumeLocation(location: Location?) {
        if (location != null) {
            locationNotice = null
            weatherViewModel.loadWeatherByCoordinates(
                latitude = location.latitude,
                longitude = location.longitude,
                showLoading = uiState !is WeatherUiState.Success,
            )
        } else {
            fallbackToCityWeather(locationFailedMessage)
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationWeather() {
        if (!hasLocationPermission(context)) {
            fallbackToCityWeather(locationPermissionDeniedMessage)
            return
        }

        val cancellationTokenSource = CancellationTokenSource()
        val timeoutHandler = Handler(Looper.getMainLooper())
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(30_000L)
            .setDurationMillis(10_000L)
            .build()
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        var hasHandledResult = false
        var nativeListener: LocationListener? = null

        fun resolveLocation(location: Location?) {
            if (hasHandledResult) return
            hasHandledResult = true
            timeoutHandler.removeCallbacksAndMessages(null)
            cancellationTokenSource.cancel()
            nativeListener?.let { listener ->
                locationManager?.let { manager -> runCatching { manager.removeUpdates(listener) } }
            }
            consumeLocation(location)
        }

        fun requestNativeLocationFallback() {
            if (hasHandledResult) return
            val manager = locationManager ?: run {
                resolveLocation(null)
                return
            }
            val providers = buildNativeLocationProviders(context, manager)
            val cachedLocation = providers
                .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
                .maxByOrNull { it.time }
            if (cachedLocation != null) {
                resolveLocation(cachedLocation)
                return
            }
            if (providers.isEmpty()) {
                resolveLocation(null)
                return
            }

            lateinit var listener: LocationListener
            listener = LocationListener { location -> resolveLocation(location) }
            nativeListener = listener
            providers.forEach { provider ->
                runCatching {
                    manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                }
            }
            timeoutHandler.postDelayed({
                val fallbackLocation = providers
                    .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
                    .maxByOrNull { it.time }
                resolveLocation(fallbackLocation)
            }, 6_000L)
        }

        timeoutHandler.postDelayed({ requestNativeLocationFallback() }, 5_000L)
        fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) resolveLocation(location) else requestNativeLocationFallback()
            }
            .addOnFailureListener { requestNativeLocationFallback() }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            showPermissionRationale = false
            showPermissionSettingsGuide = false
            if (isLocationServiceEnabled(context)) {
                showLocationServiceGuide = false
                requestLocationWeather()
            } else {
                showLocationServiceGuide = true
                fallbackToCityWeather(locationServiceDisabledMessage)
            }
        } else {
            val shouldShowRationale = activity?.let(::shouldShowLocationPermissionRationale) == true
            if (shouldShowRationale) {
                showPermissionRationale = true
                fallbackToCityWeather(locationPermissionDeniedMessage)
            } else if (hasRequestedLocationPermission) {
                showPermissionSettingsGuide = true
                fallbackToCityWeather(locationSettingsMessage)
            } else {
                fallbackToCityWeather(locationPermissionDeniedMessage)
            }
        }
    }

    fun launchLocationPermissionRequest() {
        hasRequestedLocationPermission = true
        locationPermissionLauncher.launch(locationPermissions)
    }

    fun requestLocationOrFallback() {
        if (hasLocationPermission(context)) {
            showPermissionRationale = false
            showPermissionSettingsGuide = false
            if (isLocationServiceEnabled(context)) {
                showLocationServiceGuide = false
                requestLocationWeather()
            } else {
                showLocationServiceGuide = true
                fallbackToCityWeather(locationServiceDisabledMessage)
            }
        } else {
            launchLocationPermissionRequest()
        }
    }

    LaunchedEffect(Unit) {
        weatherViewModel.restoreStartupSnapshot()
    }

    LaunchedEffect(activity) {
        if (activity != null) {
            requestLocationOrFallback()
        }
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onConfirm = {
                showPermissionRationale = false
                launchLocationPermissionRequest()
            },
            onDismiss = {
                showPermissionRationale = false
                fallbackToCityWeather(locationPermissionDeniedMessage)
            }
        )
    }
    if (showPermissionSettingsGuide) {
        PermissionSettingsDialog(
            onOpenSettings = {
                showPermissionSettingsGuide = false
                context.openAppSettings()
            },
            onDismiss = { showPermissionSettingsGuide = false }
        )
    }
    if (showLocationServiceGuide) {
        LocationServiceDialog(
            onOpenSettings = {
                showLocationServiceGuide = false
                context.openLocationSourceSettings()
            },
            onDismiss = { showLocationServiceGuide = false }
        )
    }

    when (currentPage) {
        WeatherPage.Home -> HomeWeatherScreen(
            uiState = uiState,
            homeCards = homeCityCards,
            selectedCardKey = selectedHomeCardKey,
            onSelectHomeCard = weatherViewModel::selectHomeCard,
            onOpenCityManager = weatherViewModel::openCityManager,
            onRefresh = {
                if (selectedHomeCardKey == StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY) {
                    requestLocationOrFallback()
                } else {
                    weatherViewModel.refreshVisibleHomeWeather()
                }
            },
            onRetry = {
                if (selectedHomeCardKey == StartupSnapshotStore.CURRENT_LOCATION_CARD_KEY) {
                    requestLocationOrFallback()
                } else {
                    weatherViewModel.refreshVisibleHomeWeather()
                }
            }
        )

        WeatherPage.CityManager -> CityManagerScreen(
            state = cityManagerState,
            currentLocationWeather = currentLocationWeather,
            locationNotice = locationNotice,
            onBack = weatherViewModel::returnHome,
            onSearchQueryChange = weatherViewModel::updateCityManagerSearchQuery,
            onSearch = weatherViewModel::searchCitiesForManager,
            onPreviewCandidate = weatherViewModel::previewSearchCandidate,
            onAddSelectedCity = weatherViewModel::addSelectedCityToFavorites,
            onOpenFavoriteCity = weatherViewModel::selectFavoriteCity,
            onSetDefaultFavorite = weatherViewModel::setDefaultFavoriteCity,
            onDeleteFavorite = weatherViewModel::removeFavoriteCity,
            onOpenCurrentLocation = weatherViewModel::showCurrentLocationWeather,
            onRefreshLocation = ::requestLocationOrFallback,
        )
    }
}

@Composable
private fun PermissionRationaleDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weather_permission_rationale_title)) },
        text = { Text(stringResource(R.string.weather_permission_rationale_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.weather_permission_rationale_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.weather_permission_rationale_dismiss)) } }
    )
}

@Composable
private fun PermissionSettingsDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weather_permission_settings_title)) },
        text = { Text(stringResource(R.string.weather_permission_settings_message)) },
        confirmButton = { TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.weather_permission_settings_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.weather_permission_settings_dismiss)) } }
    )
}

@Composable
private fun LocationServiceDialog(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weather_location_service_title)) },
        text = { Text(stringResource(R.string.weather_location_service_message)) },
        confirmButton = { TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.weather_location_service_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.weather_location_service_dismiss)) } }
    )
}

private fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ||
            runCatching { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
    }
}

private fun buildNativeLocationProviders(context: Context, locationManager: LocationManager): List<String> {
    val providers = mutableListOf<String>()
    val canUseFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val canUseCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (canUseFineLocation && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) providers += LocationManager.GPS_PROVIDER
    if ((canUseFineLocation || canUseCoarseLocation) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) providers += LocationManager.NETWORK_PROVIDER
    if ((canUseFineLocation || canUseCoarseLocation) && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) providers += LocationManager.PASSIVE_PROVIDER
    return providers.distinct()
}

private fun hasLocationPermission(context: Context): Boolean {
    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return coarseGranted || fineGranted
}

private fun shouldShowLocationPermissionRationale(activity: Activity): Boolean {
    return locationPermissions.any { permission -> ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

private fun Context.openLocationSourceSettings() {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
