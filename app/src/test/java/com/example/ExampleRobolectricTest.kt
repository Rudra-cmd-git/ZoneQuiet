package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GeofenceZone
import com.example.ui.map.OsmTileHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZoneQuiet", appName)
    }

    @Test
    fun `osm tile helper converts coordinates to world and back accurately`() {
        val lat = 37.7749
        val lon = -122.4194
        val zoom = 15

        val (worldX, worldY) = OsmTileHelper.latLonToWorld(lat, lon, zoom)
        val (calcLat, calcLon) = OsmTileHelper.worldToLatLon(worldX, worldY, zoom)

        assertEquals(lat, calcLat, 0.001)
        assertEquals(lon, calcLon, 0.001)
    }

    @Test
    fun `osm tile helper calculates reasonable meters to pixels for radius`() {
        val lat = 37.7749
        val zoom = 15
        val radiusMeters = 250f

        val pixels = OsmTileHelper.metersToPixels(radiusMeters, lat, zoom)
        assertTrue("Radius pixels should be greater than 20 at zoom 15", pixels > 20f)
    }
}
