package com.example.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datastore.ZonePreferencesDataStore
import com.example.data.model.ActivityLogEntry
import com.example.data.model.GeofenceZone
import com.example.data.model.LogType
import com.example.data.model.PermissionState
import com.example.data.model.SearchResult
import com.example.data.model.TransitionState
import com.example.data.network.LocationSearchService
import com.example.service.DndNotificationHelper
import com.example.service.GeofenceManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = ZonePreferencesDataStore(application)
    private val geofenceManager = GeofenceManager(application)
    private val dndHelper = DndNotificationHelper(application)
    private val searchService = LocationSearchService()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    val zoneState: StateFlow<GeofenceZone> = dataStore.zoneFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GeofenceZone()
        )

    val isAutomationEnabled: StateFlow<Boolean> = dataStore.isAutomationEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val wasDndPriorActive: StateFlow<Boolean> = dataStore.wasDndPriorActiveFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val currentTransitionState: StateFlow<TransitionState> = dataStore.currentZoneStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransitionState.UNKNOWN
        )

    val activityLogs: StateFlow<List<ActivityLogEntry>> = dataStore.activityLogsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _permissionsState = MutableStateFlow(PermissionState())
    val permissionsState: StateFlow<PermissionState> = _permissionsState.asStateFlow()

    private val _isDndActive = MutableStateFlow(false)
    val isDndActive: StateFlow<Boolean> = _isDndActive.asStateFlow()

    private val _dndStatusDescription = MutableStateFlow("Unknown")
    val dndStatusDescription: StateFlow<String> = _dndStatusDescription.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkPermissions()
        refreshDndState()
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasBg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasPolicy = dndHelper.hasNotificationPolicyAccess()

        val hasPost = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _permissionsState.value = PermissionState(
            hasFineLocation = hasFine,
            hasBackgroundLocation = hasBg,
            hasNotificationPolicy = hasPolicy,
            hasPostNotifications = hasPost
        )
    }

    fun refreshDndState() {
        _isDndActive.value = dndHelper.isDndActive()
        _dndStatusDescription.value = dndHelper.getDndStatusDescription()
    }

    fun updateLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val currentZone = zoneState.value
            val updated = currentZone.copy(latitude = lat, longitude = lon)
            dataStore.saveZone(updated)

            // If automation is currently enabled, re-register geofence with new coordinates
            if (updated.isEnabled) {
                geofenceManager.registerGeofence(updated)
                dataStore.addLog(
                    title = "Geofence Updated",
                    detail = "Coordinates set to ${String.format("%.4f", lat)}, ${String.format("%.4f", lon)}",
                    type = LogType.SYSTEM
                )
            }
        }
    }

    fun updateRadius(radiusMeters: Float) {
        viewModelScope.launch {
            val currentZone = zoneState.value
            val updated = currentZone.copy(radiusMeters = radiusMeters)
            dataStore.saveZone(updated)

            if (updated.isEnabled) {
                geofenceManager.registerGeofence(updated)
            }
        }
    }

    fun updateLabel(label: String) {
        viewModelScope.launch {
            val currentZone = zoneState.value
            val updated = currentZone.copy(label = label)
            dataStore.saveZone(updated)
        }
    }

    fun toggleAutomation(enabled: Boolean) {
        viewModelScope.launch {
            checkPermissions()
            val perms = _permissionsState.value

            if (enabled && !perms.hasFineLocation) {
                _errorMessage.value = "Precise location permission is required."
                return@launch
            }
            if (enabled && !perms.hasNotificationPolicy) {
                _errorMessage.value = "Do Not Disturb access is required to automate silencing."
                return@launch
            }

            val currentZone = zoneState.value
            val updated = currentZone.copy(isEnabled = enabled)
            dataStore.saveZone(updated)
            dataStore.setAutomationEnabled(enabled)

            if (enabled) {
                val result = geofenceManager.registerGeofence(updated)
                if (result.isSuccess) {
                    dataStore.addLog(
                        title = "Automation Enabled",
                        detail = "ZoneQuiet is actively monitoring '${updated.label}' (${updated.radiusMeters.toInt()}m).",
                        type = LogType.SYSTEM
                    )
                } else {
                    _errorMessage.value = "Failed to register geofence: ${result.exceptionOrNull()?.message}"
                    dataStore.addLog(
                        title = "Geofence Registration Failed",
                        detail = "${result.exceptionOrNull()?.message}",
                        type = LogType.ERROR
                    )
                }
            } else {
                geofenceManager.removeGeofence()
                dataStore.setCurrentZoneState(TransitionState.UNKNOWN)
                dataStore.addLog(
                    title = "Automation Disabled",
                    detail = "Geofence monitoring stopped by user.",
                    type = LogType.SYSTEM
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentGpsLocation() {
        checkPermissions()
        if (!_permissionsState.value.hasFineLocation) {
            _errorMessage.value = "Location permission is required to detect current location."
            return
        }

        viewModelScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await() ?: fusedLocationClient.lastLocation.await()

                if (location != null) {
                    updateLocation(location.latitude, location.longitude)
                    dataStore.addLog(
                        title = "GPS Location Updated",
                        detail = "Centered on current GPS coordinates (${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}).",
                        type = LogType.SYSTEM
                    )
                } else {
                    _errorMessage.value = "Could not retrieve GPS coordinates. Ensure device location is turned on."
                }
            } catch (e: Exception) {
                _errorMessage.value = "GPS Error: ${e.message}"
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
        }
    }

    fun performSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = searchService.searchLocation(query)
                _searchResults.value = results
                if (results.isEmpty()) {
                    _errorMessage.value = "No locations found for '$query'"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search error: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectSearchResult(result: SearchResult) {
        updateLocation(result.latitude, result.longitude)
        // Also update label if user has default label
        if (zoneState.value.label == "Quiet Zone") {
            val shortName = result.displayName.split(",").firstOrNull() ?: result.displayName
            updateLabel(shortName.trim())
        }
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun clearLogs() {
        viewModelScope.launch {
            dataStore.clearLogs()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
