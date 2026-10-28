package com.example.data.model

enum class TransitionState {
    INSIDE,
    OUTSIDE,
    UNKNOWN
}

enum class LogType {
    ENTER,
    EXIT,
    DND_ON,
    DND_OFF,
    PRESERVED,
    ERROR,
    SYSTEM
}

data class GeofenceZone(
    val id: String = "zonequiet_primary_zone",
    val latitude: Double = 37.7749, // Default San Francisco or user location
    val longitude: Double = -122.4194,
    val radiusMeters: Float = 250f,
    val label: String = "Quiet Zone",
    val isEnabled: Boolean = false
)

data class ActivityLogEntry(
    val id: String,
    val timestamp: Long,
    val title: String,
    val detail: String,
    val type: LogType
)

data class PermissionState(
    val hasFineLocation: Boolean = false,
    val hasBackgroundLocation: Boolean = false,
    val hasNotificationPolicy: Boolean = false,
    val hasPostNotifications: Boolean = false
) {
    val isAllGranted: Boolean
        get() = hasFineLocation && hasBackgroundLocation && hasNotificationPolicy && hasPostNotifications

    val canRunForegroundOnly: Boolean
        get() = hasFineLocation && hasNotificationPolicy
}

data class SearchResult(
    val placeId: Long,
    val displayName: String,
    val latitude: Double,
    val longitude: Double
)
