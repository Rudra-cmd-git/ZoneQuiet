package com.example.ui.map

import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

object OsmTileHelper {
    const val TILE_SIZE = 256

    /**
     * Converts longitude and latitude to continuous world coordinates at a given zoom level.
     */
    fun latLonToWorld(lat: Double, lon: Double, zoom: Int): Pair<Double, Double> {
        val n = 2.0.pow(zoom)
        val x = (lon + 180.0) / 360.0 * n * TILE_SIZE
        val latRad = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        val y = (1.0 - (ln(tan(latRad) + 1.0 / cos(latRad)) / PI)) / 2.0 * n * TILE_SIZE
        return Pair(x, y)
    }

    /**
     * Converts world pixel coordinates back to latitude and longitude.
     */
    fun worldToLatLon(worldX: Double, worldY: Double, zoom: Int): Pair<Double, Double> {
        val n = 2.0.pow(zoom)
        val lon = (worldX / (n * TILE_SIZE)) * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1.0 - 2.0 * worldY / (n * TILE_SIZE))))
        val lat = Math.toDegrees(latRad)
        return Pair(lat.coerceIn(-85.0511, 85.0511), lon.coerceIn(-180.0, 180.0))
    }

    /**
     * Computes the number of meters per pixel at a given latitude and zoom level.
     */
    fun metersPerPixel(lat: Double, zoom: Int): Double {
        val latRad = Math.toRadians(lat)
        return 156543.03392 * cos(latRad) / 2.0.pow(zoom)
    }

    /**
     * Converts a radius in meters to radius in screen pixels.
     */
    fun metersToPixels(radiusMeters: Float, lat: Double, zoom: Int): Float {
        val mpp = metersPerPixel(lat, zoom)
        return if (mpp > 0) (radiusMeters / mpp).toFloat() else 0f
    }

    /**
     * Formats OpenStreetMap standard tile URL.
     */
    fun getTileUrl(tileX: Int, tileY: Int, zoom: Int): String {
        return "https://tile.openstreetmap.org/$zoom/$tileX/$tileY.png"
    }
}
