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
    const val BASE_TILE_SIZE = 256

    /**
     * Converts longitude and latitude to continuous world coordinates at a given zoom level and screen tile size in pixels.
     */
    fun latLonToWorld(lat: Double, lon: Double, zoom: Int, tileSizePx: Float = 256f): Pair<Double, Double> {
        val n = 2.0.pow(zoom)
        val x = (lon + 180.0) / 360.0 * n * tileSizePx
        val latRad = Math.toRadians(lat.coerceIn(-85.0511, 85.0511))
        val y = (1.0 - (ln(tan(latRad) + 1.0 / cos(latRad)) / PI)) / 2.0 * n * tileSizePx
        return Pair(x, y)
    }

    /**
     * Converts world pixel coordinates back to latitude and longitude.
     */
    fun worldToLatLon(worldX: Double, worldY: Double, zoom: Int, tileSizePx: Float = 256f): Pair<Double, Double> {
        val n = 2.0.pow(zoom)
        val lon = (worldX / (n * tileSizePx)) * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1.0 - 2.0 * worldY / (n * tileSizePx))))
        val lat = Math.toDegrees(latRad)
        return Pair(lat.coerceIn(-85.0511, 85.0511), lon.coerceIn(-180.0, 180.0))
    }

    /**
     * Computes the number of meters per pixel at a given latitude, zoom level, and screen tile size.
     */
    fun metersPerPixel(lat: Double, zoom: Int, tileSizePx: Float = 256f): Double {
        val latRad = Math.toRadians(lat)
        val scale = tileSizePx / BASE_TILE_SIZE.toDouble()
        return (156543.03392 * cos(latRad) / 2.0.pow(zoom)) / scale
    }

    /**
     * Converts a radius in meters to radius in screen pixels.
     */
    fun metersToPixels(radiusMeters: Float, lat: Double, zoom: Int, tileSizePx: Float = 256f): Float {
        val mpp = metersPerPixel(lat, zoom, tileSizePx)
        return if (mpp > 0) (radiusMeters / mpp).toFloat() else 0f
    }

    /**
     * High-speed, dark-themed CartoDB map tile CDN (robust for mobile apps and dark UI).
     */
    fun getCartoDbDarkTileUrl(tileX: Int, tileY: Int, zoom: Int): String {
        val subdomains = listOf("a", "b", "c", "d")
        val s = subdomains[(tileX + tileY) % subdomains.size]
        return "https://$s.basemaps.cartocdn.com/rastertiles/dark_all/$zoom/$tileX/$tileY.png"
    }

    /**
     * Standard OpenStreetMap tile URL (fallback).
     */
    fun getOsmTileUrl(tileX: Int, tileY: Int, zoom: Int): String {
        return "https://tile.openstreetmap.org/$zoom/$tileX/$tileY.png"
    }
}
