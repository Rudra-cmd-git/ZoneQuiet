# Fix Map Tile Alignment & Native Location Search

A comprehensive fix to resolve map rendering issues (tile overlap, gray tiles, and misalignment) and repair the location search functionality using Android's native Geocoder and dual-mode search.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following issues and solutions were identified during clarification:
> 1. **Map Display (Blank Gray / Misaligned Tiles)**:
>    - *Cause*: Screen pixel density mismatch (drawing 256dp views using raw 256px world coordinates caused severe tile scaling and positioning errors on high-DPI devices), compounded by OSM tile server 403 blocks.
>    - *Solution*: Correct pixel-to-density conversion using `LocalDensity`, switch to reliable, high-speed CartoDB Dark Matter tiles natively styled for OLED dark mode, and include a vector radar background grid so the map remains responsive and beautiful under all network conditions.
> 2. **Location Search ("No locations found" / Not triggering)**:
>    - *Cause*: Nominatim API network blocks, lack of an explicit tap-to-search button, and unhandled geocoding exceptions.
>    - *Solution*: Integrate Android's native `android.location.Geocoder` (powered by Google Play Services on device) with fallback to Nominatim, add an explicit search action button inside the text field, and auto-trigger on keyboard action.

---

### 1. Overview & Core Concept

- **What It Fixes**: 
  1. Fixes the interactive map canvas so tiles align seamlessly, scale proportionally, and pan/zoom fluidly at any DPI.
  2. Ensures location search returns accurate landmarks, cities, and addresses with immediate visual tap-to-pin navigation.
- **Key Value**: Provides a reliable, professional-grade mapping experience with zero blank screens or failed searches.

---

### 2. User Experience & Visual Design

1. **Interactive Map Display**:
   - Modern CartoDB Dark Matter map tiles designed specifically for dark UIs.
   - Vector radar grid background ensures instant feedback while tiles stream in.
   - Precise radius circle overlay in glowing emerald and electric indigo accurately scaled to zoom level.
   - Smooth pan and pinch gestures, with instant pin repositioning on map tap.

2. **Location Search Bar**:
   - Clear input with a prominent **Search Button** icon right inside the trailing bar.
   - Immediate dropdown suggestions displaying place names and formatted coordinates.
   - Single tap on any result instantly centers the map pin and updates the geofence location.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Density-Aware Tile Geometry**
  - *Chosen Approach*: Convert Mercator world coordinates using `LocalDensity` so tile size in pixels matches tile width in DP on all screen densities (`mdpi` through `xxxhdpi`).
  - *Why*: Eliminates pixel stretching, seams, and visual overlap.

- **Decision 2: Android Native `Geocoder` + Nominatim Fallback**
  - *Chosen Approach*: First invoke `android.location.Geocoder` (on-device system service). If unavailable or returning empty, fall back to Nominatim with a verified mobile user-agent.
  - *Why*: Android's built-in Geocoder runs locally through Google Play Services, eliminating HTTP rate limits and 403 Forbidden errors.

---

### 4. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Search & Map Architecture                       │
│                                                                        │
│   LocationSearchBar                                                    │
│   ├── User types query: "Central Park, NY"                             │
│   ├── Taps search icon or press Enter                                  │
│   └── Calls `MainViewModel.performSearch()`                            │
│              │                                                         │
│              ▼                                                         │
│   LocationSearchService                                                │
│   ├── 1. `android.location.Geocoder.getFromLocationName()`             │
│   └── 2. (Fallback) HTTPS Nominatim with custom User-Agent             │
│              │                                                         │
│              ▼                                                         │
│   `MainViewModel.updateLocation(lat, lon)`                             │
│              │                                                         │
│              ▼                                                         │
│   OsmMapView                                                           │
│   ├── Density-corrected Mercator tile positioning (`tileSizeDp`)       │
│   ├── CartoDB Dark Matter / OSM tiles with cache & crossfade           │
│   └── Canvas radius circle scaled via `OsmTileHelper.metersToPixels`   │
└────────────────────────────────────────────────────────────────────────┘
```

#### Execution Steps Upon Approval

1. Update `OsmTileHelper.kt` and `OsmMapView.kt` with density-aware sizing and CartoDB dark tiles.
2. Update `LocationSearchService.kt` to use Android's native `Geocoder` with fallback.
3. Update `LocationSearchBar.kt` to add a dedicated search trigger icon button.
4. Verify with `compile_applet` and commit changes.
