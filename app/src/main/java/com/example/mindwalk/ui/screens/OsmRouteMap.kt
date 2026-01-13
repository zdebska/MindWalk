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

@Composable
fun OsmRouteMap(
    modifier: Modifier = Modifier,
    routePoints: List<GeoPoint>,
    zoom: Double = 16.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // OSMDroid needs userAgent set (important)
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
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
                if (routePoints.isNotEmpty()) {
                    controller.setCenter(routePoints.first())
                }

                mapView = this
            }
        },
        update = { map ->
            // remove old polylines
            map.overlays.removeAll { it is Polyline }

            if (routePoints.size >= 2) {
                val line = Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#5A8F75")
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                }
                map.overlays.add(line)

                // keep camera centered (optional)
                map.controller.setCenter(routePoints.first())
                map.invalidate()
            }
        }
    )

    // lifecycle bridge for MapView
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
