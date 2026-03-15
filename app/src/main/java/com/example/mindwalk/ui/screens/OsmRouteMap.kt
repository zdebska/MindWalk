package com.example.mindwalk.ui.screens

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import java.util.*

@Composable
fun OsmRouteMap(
    modifier: Modifier = Modifier,
    routePoints: List<GeoPoint>,
    zoom: Double = 16.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize configuration once
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            // Set a unique user agent as required by OSM policy
            userAgentValue = "MindWalk/1.0 (Android; ${context.packageName})"
            
            // Use internal cache directory to avoid permission issues with external storage
            val cacheDir = context.cacheDir
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir
            
            // Load existing preferences if any
            load(context, context.getSharedPreferences("osmdroid", 0))
        }
    }

    var mapView: MapView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(zoom)
                
                val defaultCenter = if (routePoints.isNotEmpty()) {
                    routePoints.first()
                } else {
                    GeoPoint(49.1951, 16.6068) // Brno
                }
                controller.setCenter(defaultCenter)

                mapView = this
            }
        },
        update = { map ->
            if (routePoints.isNotEmpty()) {
                map.controller.setCenter(routePoints.first())
            }

            map.overlays.clear()

            if (routePoints.size >= 2) {
                // Route Line
                val line = Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#5A8F75")
                    outlinePaint.strokeWidth = 12f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                }
                map.overlays.add(line)

                // Start
                map.overlays.add(Marker(map).apply {
                    position = routePoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Start"
                })

                // End
                map.overlays.add(Marker(map).apply {
                    position = routePoints.last()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "End"
                })
            }
            
            map.invalidate()
        }
    )

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            mapView?.onDetach()
            mapView = null
        }
    }
}
