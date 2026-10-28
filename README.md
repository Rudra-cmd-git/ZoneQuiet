# ZoneQuiet — Geofence Do Not Disturb Automator

**ZoneQuiet** is a smart, battery-efficient Android application built with Jetpack Compose and Google Play Services Geofencing. It automatically silences your phone (Do Not Disturb) when you enter a designated perimeter (such as an office, university lecture hall, library, or home study) and restores your normal alert mode when you leave.

Crucially, **ZoneQuiet will never disable Do Not Disturb on exit if DND was already turned ON prior to entering the area**, respecting your manual silence preferences at all times.

---

## Key Features

- **Interactive OpenStreetMap with Live Radius Circle**: Visual real-time rendering of your geofence zone with smooth pan, pinch-to-zoom, and touch-to-pin controls.
- **Current Location GPS & Address Search**: Tap "Use Current Location" to lock onto your coordinates, or type any landmark, street, or city into the search bar (powered by OpenStreetMap Nominatim).
- **Customizable Radius**: Adjust coverage from 50 meters up to 2000 meters using the slider or quick presets (100m, 250m, 500m, 1km, 1.5km).
- **Smart Prior-State Preservation**: Tracks whether DND was already active before entering. If you manually turned on DND before reaching your office, exiting will leave DND ON.
- **Hardware-Accelerated Background Geofencing**: Powered by Google Play Services `GeofencingClient` with batching, eliminating continuous GPS battery drain.
- **Device Reboot & App Update Resilience**: `BootReceiver` automatically restores geofence tracking when the device boots or when the app is updated.
- **Discreet Notifications & Activity Stream**: Real-time status notifications on transitions, plus an in-app timestamped activity log.
- **Modern OLED Dark Theme**: Designed with high-contrast obsidian, neon emerald (`#10B981`), and electric indigo (`#6366F1`) accents.

---

## Required Android Permissions

Because Android strictly regulates background location and system interruptions, the app includes an interactive step-by-step checklist guiding you through:

1. **Precise Location** (`ACCESS_FINE_LOCATION`): Required for accurate GPS boundary detection.
2. **Background Location** (`ACCESS_BACKGROUND_LOCATION`): Set to *"Allow all the time"* so geofences trigger even when the screen is locked or the app is closed.
3. **Do Not Disturb Access** (`android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS`): Grants the app permission to switch `NotificationManager.interruptionFilter`.
4. **Notifications** (`POST_NOTIFICATIONS`): For transition status banners on Android 13+.

---

## Building the APK

### Prerequisites
- JDK 17 or 21
- Android SDK with API 34+
- Gradle (or Android Studio)

### Build Steps

1. **Clone or Open the Project**:
   ```bash
   git clone <repo-url>
   cd ZoneQuiet
   ```

2. **Build Debug APK**:
   ```bash
   gradle assembleDebug
   ```
   The generated APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Build Release APK**:
   ```bash
   gradle assembleRelease
   ```
   The release APK will be located at:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

---

## Installing & Testing

### 1. Install via ADB
Connect your Android phone with USB Debugging enabled, or start an Android Virtual Device (AVD):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. Granting Permissions
1. Open **ZoneQuiet**.
2. Tap each action button in the **Permissions Required** banner:
   - Allow **Location** -> Choose *"Precise"* and *"While using the app"*.
   - Tap **Background Location** -> Choose *"Allow all the time"*.
   - Tap **Do Not Disturb Control** -> Find **ZoneQuiet** in the system list and toggle **Allow Do Not Disturb**.
   - Tap **Enable Notifications** -> Allow notifications.

### 3. Testing Geofence Transitions in Android Emulator
You can simulate entering and exiting the geofence using ADB or Emulator Extended Controls:

- **Center Location**: In the app, tap the GPS icon to set your current location as the geofence center (e.g., Lat: `37.7749`, Lon: `-122.4194`) with a `250m` radius.
- **Enable Automation**: Toggle the master **Geofence Automation** switch to **ON**.
- **Simulate Outside Location**:
  In emulator Extended Controls -> **Location**, set coordinates 1km away (e.g., `37.7650`, `-122.4194`).
- **Simulate Inside Location**:
  Set coordinates back to `37.7749`, `-122.4194`. You will see DND turn ON and a notification will appear!
- **Verify Prior DND State Memory**:
  Manually turn on DND in Quick Settings while outside. Then move inside and outside. Notice that when exiting, ZoneQuiet preserves your manual DND state!

---

## Architecture & Code Structure

- `MainActivity.kt`: Compose UI with edge-to-edge support, top app bar, and reactive state collection.
- `OsmMapView.kt` & `OsmTileHelper.kt`: Native interactive OpenStreetMap slippy canvas with real-time Mercator projection math and dynamic radius scaling.
- `GeofenceManager.kt`: Official Google Play Services `GeofencingClient` integration.
- `GeofenceBroadcastReceiver.kt`: Background event processor handling `ENTER`/`EXIT` transitions and prior state logic.
- `DndNotificationHelper.kt`: Manages `NotificationManager.interruptionFilter` and notification channels.
- `ZonePreferencesDataStore.kt`: DataStore for persisting zone configuration, DND memory, and activity logs.
- `BootReceiver.kt`: Restores geofence tracking on device reboot.
