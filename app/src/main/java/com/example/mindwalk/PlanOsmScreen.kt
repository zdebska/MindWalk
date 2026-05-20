package com.example.mindwalk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * Early prototype composable that renders a full-screen OSMDroid [MapView] centred on Prague.
 *
 * This screen is **not wired into the navigation graph** — it exists as a standalone sandbox
 * used during initial OSMDroid integration testing. Production map functionality lives in
 * [com.example.mindwalk.ui.screens.LocationPickerScreen] and
 * [com.example.mindwalk.ui.screens.PlanScreen].
 *
 * The map is initialised with:
 * - Tile source: [TileSourceFactory.MAPNIK] (standard OpenStreetMap raster tiles)
 * - Multi-touch pinch-to-zoom enabled
 * - Zoom level 13, centred at GeoPoint(50.0755, 14.4378) — central Prague
 */
@Composable
fun PlanOsmScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(50.0755, 14.4378)) // Prague
                }
            }
        )
    }
}
