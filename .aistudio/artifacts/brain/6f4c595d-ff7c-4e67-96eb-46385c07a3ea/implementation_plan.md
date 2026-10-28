# Remove Google Maps Secrets Plugin & Ensure Zero-API-Key Operation

A focused plan to remove the Google Maps Platform Secrets Gradle plugin and its manifest injection from `app/build.gradle.kts`, ensuring that the app runs on 100% free OpenStreetMap and on-device geocoding with absolutely zero API key requirements.

### User Review & Critical Decisions

> [!IMPORTANT]
> - **Identified Cause of API Key Prompt**: 
>   The starter template included `com.google.android.libraries.mapsplatform.secrets-gradle-plugin` (`alias(libs.plugins.secrets)`). This plugin specifically looks for a Google Maps API Key (`MAPS_API_KEY`) and injects metadata into the application package.
> - **Confirmed Solution**:
>   1. Completely remove `alias(libs.plugins.secrets)` and the `secrets { ... }` block from `app/build.gradle.kts`.
>   2. Guarantee that the APK has zero Google Maps metadata and operates purely on OpenStreetMap tiles and Android native Geocoder.
>   3. Add an explicit label on the map view confirming: "Free OpenStreetMap • Zero API key required".

---

### 1. Overview & Core Concept

- **What It Fixes**: 
  1. Prevents any Google Maps API Key prompt or warning dialog on the device after installing the APK.
  2. Strips out unused Google Maps Secrets plugin configuration.
  3. Rebuilds the v1.0.1 APK with zero API key dependencies.
- **Key Value**: The application is 100% plug-and-play on any Android phone without creating a Google Cloud account, billing account, or generating API keys.

---

### 2. User Experience After Fix

1. **Installation**:
   - Install `ZoneQuiet-v1.0.1.apk`.
   - The app opens immediately without requesting any Map API key or showing Google Play API key prompts.
2. **Interactive Map**:
   - CartoDB / OpenStreetMap dark tiles load immediately with zero configuration.
   - A discreet status badge indicates: "Free OpenStreetMap (No API key required)".
3. **Location Search**:
   - Works directly through the on-device Geocoder and Nominatim without keys.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision: Complete Removal of Google Maps Secrets Plugin**
  - *Chosen Approach*: Remove `alias(libs.plugins.secrets)` and the `secrets { ... }` block from `app/build.gradle.kts`.
  - *Why*: ZoneQuiet does not use Google Maps SDK (which requires paid Google Cloud billing and API keys); it uses free OpenStreetMap and CartoDB. Removing the plugin prevents manifest pollution and removes all API key checks.

---

### 4. Technical Architecture & Component Changes

```
┌────────────────────────────────────────────────────────────────────────┐
│                        `app/build.gradle.kts`                          │
│                                                                        │
│   BEFORE:                                                              │
│   ├── `alias(libs.plugins.secrets)`  <-- Maps Platform Secrets Plugin  │
│   └── `secrets { propertiesFileName = ".env" ... }`                    │
│                                                                        │
│   AFTER:                                                               │
│   ├── Removed `alias(libs.plugins.secrets)`                            │
│   └── Removed `secrets { ... }` block                                  │
│                                                                        │
│   RESULT: Clean package without Google Maps API key requirements       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Reassembled APK & Git                           │
│                                                                        │
│   1. Rebuild APK: `gradle assembleDebug`                               │
│   2. Commit changes to `main`                                          │
│   3. Update git tag `v1.0.1`                                           │
└────────────────────────────────────────────────────────────────────────┘
```

#### Execution Steps Upon Approval

1. Edit `app/build.gradle.kts` to remove `alias(libs.plugins.secrets)` and the `secrets { ... }` block.
2. Add a clear "No API key required" indicator in `OsmMapView.kt`.
3. Assemble the updated APK with `gradle assembleDebug`.
4. Commit to git and update tag `v1.0.1`.
