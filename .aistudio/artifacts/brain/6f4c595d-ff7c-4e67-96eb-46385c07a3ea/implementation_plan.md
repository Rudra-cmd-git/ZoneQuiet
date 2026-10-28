# Geofence DND Automator (ZoneQuiet)

An intelligent, battery-efficient Android application built with Jetpack Compose that automates Do Not Disturb (DND) modes based on user-defined geofence perimeters, while strictly preserving manual DND preferences.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed during Phase 1 clarification:
> - **Location & Map Display**: Interactive OpenStreetMap map with real-time radius circle visualization, pin dragging, current GPS auto-centering, and location search (via Nominatim OSM geocoding).
> - **Notification & Status Feedback**: Discreet status banner with entry and exit confirmation notifications, plus quick action to open notification policy settings if needed.
> - **Visual Identity & Theme**: Modern OLED deep dark theme with vibrant emerald (`#10B981`) and electric indigo (`#6366F1`) accents for maximum visual contrast and battery efficiency.

---

### 1. Overview & Core Concept

- **What It Does**: Automates toggling Do Not Disturb (DND) on and off based on high-precision, low-power background geofencing around user-selected locations (work, study spaces, home, libraries, places of worship).
- **Target Audience & Persona**: Students, professionals, and focus-driven individuals who want zero distraction in specific zones without having to remember to silence their phone—and without accidentally un-muting a device they had intentionally silenced.
- **Key Value**: 
  - **Zero Effort Focus**: Automatically engages DND upon entering the configured radius.
  - **Intelligent State Preservation**: Crucially tracks whether DND was already active *before* entering the zone; if the user had manually enabled DND beforehand, leaving the zone will **never** disable it.
  - **Battery-Optimized Background Execution**: Uses Google Play Services Geofencing API hardware-accelerated batching, avoiding continuous GPS drain.
  - **Resilience**: Automatically re-registers geofences on device reboot (`RECEIVE_BOOT_COMPLETED`) and handles GPS toggles and permission revocations gracefully.

---

### 2. User Experience & Visual Design

#### Key User Flows

1. **Onboarding & Permission Checklist**:
   - On first launch, the app displays a clear, step-by-step permission card highlighting three necessary Android capabilities:
     1. *Location Permission* (`ACCESS_FINE_LOCATION`)
     2. *Background Location Permission* (`ACCESS_BACKGROUND_LOCATION`)
     3. *Do Not Disturb Access* (`android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS`)
     4. *Notifications* (`POST_NOTIFICATIONS` for Android 13+)
   - Interactive status chips indicate granted vs. missing permissions with direct single-tap deep-links to system settings.

2. **Geofence Zone Configuration**:
   - **Interactive Map**: Users can drag and tap to position the target point or tap "Use Current Location" to center the map on their live coordinates.
   - **Visual Radius Adjuster**: Interactive slider (50m to 2000m) with live visual circle scaling overlaid directly on the map.
   - **Location Search**: Quick search bar to look up addresses or points of interest with instant autocomplete.
   - **Zone Metadata**: Label the zone (e.g., "Office", "University Library", "Studio").

3. **Automation & Monitoring Dashboard**:
   - Master Automation Toggle switch with live state indicator ("Active & Monitoring", "Inside Geofence - DND On", "Outside Geofence - DND Off", "Permissions Required").
   - Live DND status badge reflecting the actual system `NotificationManager.currentInterruptionFilter`.
   - Real-time event log tracking transitions (e.g., "Entered Office geofence at 09:15 AM - DND engaged", "Exited Office geofence at 05:30 PM - DND restored").

4. **Safety & Prior State Preservation**:
   - When entering the geofence, the app records `wasDndEnabledPriorToEntry`.
   - If the user was already in DND mode prior to entry, exiting the geofence logs "Prior DND state preserved: DND remains ON".

#### Visual Identity & Theme Tokens

- **Aesthetic Direction**: Sleek OLED Dark Mode with frosted glass surfaces (`tonalElevation` and semi-transparent dark containers) and high-visibility neon accents.
- **Color Palette**:
  - `Background`: `#0A0D14` (Deep obsidian)
  - `Surface`: `#121826` (Subtle navy dark)
  - `SurfaceVariant`: `#1B2438` (Elevated card container)
  - `Primary / Accent Emerald`: `#10B981` (Focus active, zone ready)
  - `Secondary / Electric Indigo`: `#6366F1` (Geofence perimeter, location indicators)
  - `DND Active Highlight`: `#8B5CF6` (Vibrant purple for active silence mode)
  - `Text Primary`: `#F8FAFC`
  - `Text Secondary`: `#94A3B8`
  - `Error / Warning`: `#F43F5E`
- **Typography**: Clean, geometric sans-serif styling with prominent numeric metrics (radius, coordinates, distances) in monospace/medium weights for visual clarity.
- **Interactive Feedback**: Spring animations on radius changes, smooth camera panning to coordinates, and haptic feedback on toggle triggers.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Google Play Services Geofencing API vs. Foreground Location Polling**
  - *Chosen Approach*: Official `GeofencingClient` from Play Services Location with `PendingIntent` routed to a BroadcastReceiver.
  - *Why*: Eliminates battery drain caused by continuous GPS polling and complies with Android background execution limits.
  - *Alternatives Considered*: Foreground service with ongoing GPS updates, which wastes battery and clutters the notification bar.

- **Decision 2: Native Compose Slippy Map with OpenStreetMap vs. External WebView**
  - *Chosen Approach*: Native Compose interactive Slippy Map using tile rendering and canvas graphics with touch gestures (pan, pinch-to-zoom, tap-to-pin, radius circle overlay).
  - *Why*: 100% native performance, fluid recomposition, zero Google Maps API key requirements, and instant rendering on emulators and offline tile caches.
  - *Alternatives Considered*: Google Maps SDK (requires proprietary API key and setup) or WebView OSM (sluggish touch response and styling mismatch).

- **Decision 3: Settings & State Persistence**
  - *Chosen Approach*: `DataStore` preferences for settings, active geofence parameters, and `wasDndEnabledPriorToEntry` state tracking, complemented by an in-memory/DataStore activity log.
  - *Why*: Lightweight, non-blocking, asynchronous Flow-based reactive architecture that survives app kills, restarts, and reboots without heavy database migrations.

---

### 4. Technical Architecture & Data Strategy

#### System & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  ┌──────────────────────┐  ┌─────────────────┐  ┌───────────────────┐  │
│  │ Permission Checklist │  │ Interactive Map │  │ Status Dashboard  │  │
│  │  & Quick Settings    │  │ & Radius Circle │  │  & Activity Log   │  │
│  └──────────┬───────────┘  └────────┬────────┘  └─────────┬─────────┘  │
└─────────────┼───────────────────────┼─────────────────────┼────────────┘
              │                       │                     │             
              ▼                       ▼                     ▼             
┌────────────────────────────────────────────────────────────────────────┐
│                        MainViewModel & State                           │
│  - zoneState: Latitude, Longitude, Radius, Name                        │
│  - isAutomationEnabled: Boolean                                        │
│  - permissionStatus: Location, Background, DND, Notification           │
│  - recentLogs: List<ActivityLogEntry>                                  │
└──────────────────┬───────────────────────────────┬─────────────────────┘
                   │                               │                      
                   ▼                               ▼                      
┌─────────────────────────────────────┐  ┌───────────────────────────────┐
│       GeofenceManager               │  │    DndNotificationHelper      │
│  - Play Services GeofencingClient   │  │  - NotificationManager        │
│  - addGeofence(lat, lng, radius)    │  │  - isDndAccessGranted()       │
│  - removeGeofence()                 │  │  - setDndEnabled(Boolean)     │
└──────────────────┬──────────────────┘  │  - getCurrentInterruption()   │
                   │                     └───────────────┬───────────────┘
                   │ Intent                              │                
                   ▼                                     │                
┌────────────────────────────────────────────────────────▼───────────────┐
│                    GeofenceBroadcastReceiver                           │
│  - Receives GEOFENCE_TRANSITION_ENTER & GEOFENCE_TRANSITION_EXIT       │
│  - Checks prior DND state from DataStore                               │
│  - Toggles NotificationManager interruption filter                     │
│  - Sends discreet status notification & appends event log              │
└──────────────────┬─────────────────────────────────────────────────────┘
                   │                                                      
                   ▼                                                      
┌────────────────────────────────────────────────────────────────────────┐
│                       BootReceiver & Persistence                       │
│  - BootReceiver (RECEIVE_BOOT_COMPLETED): Re-registers geofence        │
│  - ZonePreferencesDataStore: Coordinates, radius, toggle state, logs   │
└────────────────────────────────────────────────────────────────────────┘
```

#### Core Data Entities

- `GeofenceZone`:
  ```kotlin
  data class GeofenceZone(
      val id: String = "active_dnd_zone",
      val latitude: Double,
      val longitude: Double,
      val radiusMeters: Float,
      val label: String,
      val isEnabled: Boolean
  )
  ```
- `DndStateRecord`:
  ```kotlin
  data class DndStateRecord(
      val wasDndAlreadyActiveBeforeEntry: Boolean,
      val lastTransition: String, // "ENTERED", "EXITED", "IDLE"
      val timestamp: Long
  )
  ```
- `ActivityLogEntry`:
  ```kotlin
  data class ActivityLogEntry(
      val id: String,
      val timestamp: Long,
      val title: String,
      val detail: String,
      val type: LogType // ENTER, EXIT, MANUAL_OVERRIDE, SYSTEM
  )
  ```

#### Key Sequences & State Transitions

1. **Zone Entry Transition**:
   - `GeofenceBroadcastReceiver` intercepts `GEOFENCE_TRANSITION_ENTER`.
   - Reads current `NotificationManager.currentInterruptionFilter`.
   - If filter is already `PRIORITY`, `ALARMS`, or `NONE` (DND already on), records `wasDndAlreadyActiveBeforeEntry = true` and leaves DND intact.
   - If filter is `ALL` (normal sound/vibrate), sets filter to `PRIORITY` (or user's chosen DND level) and records `wasDndAlreadyActiveBeforeEntry = false`.
   - Posts a discreet status notification: "Do Not Disturb turned ON (Entered Office zone)".

2. **Zone Exit Transition**:
   - `GeofenceBroadcastReceiver` intercepts `GEOFENCE_TRANSITION_EXIT`.
   - Reads `wasDndAlreadyActiveBeforeEntry`.
   - **Crucial Rule**: If `wasDndAlreadyActiveBeforeEntry == true`, does NOT disable DND! Logs and notifies: "DND preserved (was enabled prior to entry)".
   - If `false`, restores interruption filter to `INTERRUPTION_FILTER_ALL`.
   - Posts status notification: "Do Not Disturb turned OFF (Exited Office zone)".

3. **Reboot Resilience**:
   - `BootReceiver` listens to `ACTION_BOOT_COMPLETED`.
   - Reads saved geofence configuration from `ZonePreferencesDataStore`.
   - If automation is enabled, immediately schedules geofence registration with `GeofencingClient`.
