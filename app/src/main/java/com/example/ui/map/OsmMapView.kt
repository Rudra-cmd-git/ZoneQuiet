package com.example.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.IndigoNeon
import com.example.ui.theme.ObsidianBg
import kotlin.math.floor
import kotlin.math.roundToInt

@Composable
fun OsmMapView(
    targetLatitude: Double,
    targetLongitude: Double,
    radiusMeters: Float,
    isInsideZone: Boolean,
    onLocationSelected: (Double, Double) -> Unit,
    onUseCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableIntStateOf(15) }
    var centerLat by remember { mutableDoubleStateOf(targetLatitude) }
    var centerLon by remember { mutableDoubleStateOf(targetLongitude) }

    LaunchedEffect(targetLatitude, targetLongitude) {
        centerLat = targetLatitude
        centerLon = targetLongitude
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = if (isInsideZone) EmeraldNeon else IndigoNeon
    val density = LocalDensity.current
    val context = LocalContext.current

    // Tile size in DP and pixels
    val tileSizeDp = 256.dp
    val tileSizePx = with(density) { tileSizeDp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ObsidianBg)
            .testTag("osm_map_container")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val screenCenterX = widthPx / 2f
        val screenCenterY = heightPx / 2f

        val (camWorldX, camWorldY) = OsmTileHelper.latLonToWorld(centerLat, centerLon, zoomLevel, tileSizePx)

        val minWorldX = camWorldX - screenCenterX
        val maxWorldX = camWorldX + screenCenterX
        val minWorldY = camWorldY - screenCenterY
        val maxWorldY = camWorldY + screenCenterY

        val minTileX = floor(minWorldX / tileSizePx).toInt()
        val maxTileX = floor(maxWorldX / tileSizePx).toInt()
        val minTileY = floor(minWorldY / tileSizePx).toInt()
        val maxTileY = floor(maxWorldY / tileSizePx).toInt()

        val maxTiles = 1 shl zoomLevel

        // Interactive touch gesture layer (pan + tap)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(zoomLevel, centerLat, centerLon, tileSizePx) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newWorldX = camWorldX - dragAmount.x
                        val newWorldY = camWorldY - dragAmount.y
                        val (newLat, newLon) = OsmTileHelper.worldToLatLon(newWorldX, newWorldY, zoomLevel, tileSizePx)
                        centerLat = newLat
                        centerLon = newLon
                    }
                }
                .pointerInput(zoomLevel, centerLat, centerLon, tileSizePx) {
                    detectTapGestures { tapOffset ->
                        val tappedWorldX = minWorldX + tapOffset.x
                        val tappedWorldY = minWorldY + tapOffset.y
                        val (tapLat, tapLon) = OsmTileHelper.worldToLatLon(tappedWorldX, tappedWorldY, zoomLevel, tileSizePx)
                        onLocationSelected(tapLat, tapLon)
                    }
                }
        ) {
            // Fallback Radar Grid Canvas (shows subtle coordinate grid lines)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSpacing = tileSizePx / 2f
                var gx = (((-minWorldX) % gridSpacing + gridSpacing) % gridSpacing).toFloat()
                while (gx < widthPx) {
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(gx, 0f),
                        end = Offset(gx, heightPx),
                        strokeWidth = 1f
                    )
                    gx += gridSpacing
                }
                var gy = (((-minWorldY) % gridSpacing + gridSpacing) % gridSpacing).toFloat()
                while (gy < heightPx) {
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(0f, gy),
                        end = Offset(widthPx, gy),
                        strokeWidth = 1f
                    )
                    gy += gridSpacing
                }
            }

            // Render Map Tiles
            for (tx in minTileX..maxTileX) {
                for (ty in minTileY..maxTileY) {
                    val normalizedTileX = ((tx % maxTiles) + maxTiles) % maxTiles
                    if (ty in 0 until maxTiles) {
                        val tileLeftPx = (tx * tileSizePx) - minWorldX
                        val tileTopPx = (ty * tileSizePx) - minWorldY

                        val tileUrl = OsmTileHelper.getCartoDbDarkTileUrl(normalizedTileX, ty, zoomLevel)

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tileUrl)
                                .addHeader("User-Agent", "ZoneQuiet-Android/1.0 (Location DND)")
                                .crossfade(200)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(tileSizeDp)
                                .offset {
                                    IntOffset(
                                        tileLeftPx.roundToInt(),
                                        tileTopPx.roundToInt()
                                    )
                                }
                        )
                    }
                }
            }

            // Canvas for Geofence Target, Radius Circle, and Pulse
            val (targetWorldX, targetWorldY) = OsmTileHelper.latLonToWorld(targetLatitude, targetLongitude, zoomLevel, tileSizePx)
            val targetScreenX = (targetWorldX - minWorldX).toFloat()
            val targetScreenY = (targetWorldY - minWorldY).toFloat()

            val baseRadiusPx = OsmTileHelper.metersToPixels(radiusMeters, targetLatitude, zoomLevel, tileSizePx)
            val currentRadiusPx = baseRadiusPx * pulseScale

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(targetScreenX, targetScreenY)

                // Fill area of geofence
                drawCircle(
                    color = primaryColor.copy(alpha = 0.20f),
                    radius = currentRadiusPx,
                    center = centerOffset
                )

                // Perimeter outline
                drawCircle(
                    color = primaryColor.copy(alpha = 0.85f),
                    radius = currentRadiusPx,
                    center = centerOffset,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Inner guide ring
                drawCircle(
                    color = primaryColor.copy(alpha = 0.35f),
                    radius = (currentRadiusPx * 0.5f),
                    center = centerOffset,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Center Pin Marker
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (targetScreenX - 18.dp.toPx()).roundToInt(),
                            (targetScreenY - 36.dp.toPx()).roundToInt()
                        )
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Selected geofence center",
                            tint = ObsidianBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Top Status pill showing active radius and coordinates
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ObsidianBg.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Radius: ${radiusMeters.roundToInt()}m  |  ${String.format("%.4f, %.4f", targetLatitude, targetLongitude)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                }
            }

            // Zoom In / Out controls (top right)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (zoomLevel < 18) zoomLevel += 1
                    },
                    containerColor = ObsidianBg.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                SmallFloatingActionButton(
                    onClick = {
                        if (zoomLevel > 3) zoomLevel -= 1
                    },
                    containerColor = ObsidianBg.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
            }

            // Bottom Right controls: Center on Pin & GPS Location
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        centerLat = targetLatitude
                        centerLon = targetLongitude
                    },
                    containerColor = ObsidianBg.copy(alpha = 0.9f),
                    contentColor = primaryColor,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.testTag("recenter_pin_button")
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Recenter on Pin")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onUseCurrentLocation,
                    containerColor = primaryColor,
                    contentColor = ObsidianBg,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("my_location_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Use Current Location")
                }
            }
        }
    }
}
