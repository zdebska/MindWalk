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

/**
 * Composable that renders a static route preview on an OSMDroid [MapView].
 *
 * Displays the full route polyline in green with start and end [Marker]s. The map re-centres
 * and redraws automatically whenever [routePoints] changes, because the `update` lambda of
 * [AndroidView] is called on every recomposition triggered by a new point list.
 *
 * Lifecycle events are observed via [LifecycleEventObserver] so the underlying [MapView]
 * correctly pauses and resumes tile downloading when the app is backgrounded.
 *
 * Used by [RoutePreviewScreen]. For the active walking view with GPS tracking, see
 * the `NavigatorMap` composable in [WalkingScreen].
 *
 * @param modifier    Layout modifier applied to the full-screen map container.
 * @param routePoints Ordered list of [GeoPoint]s forming the route polyline.
 * @param zoom        Initial map zoom level (default 16.0).
 */
@Composable
fun OsmRouteMap(
    modifier: Modifier = Modifier,
    routePoints: List<GeoPoint>,
    zoom: Double = 16.0
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // OSMDroid configuration must be applied before the MapView is created
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue    = "MindWalk/1.0 (Android; ${context.packageName})"
            // Use internal cache to avoid requiring WRITE_EXTERNAL_STORAGE permission
            val cacheDir      = context.cacheDir
            osmdroidBasePath  = cacheDir
            osmdroidTileCache = cacheDir
            load(context, context.getSharedPreferences("osmdroid", 0))
        }
    }

    var mapView: MapView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory  = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoom)
                // Centre on the first route point, or Brno as a fallback
                controller.setCenter(routePoints.firstOrNull() ?: GeoPoint(49.1951, 16.6068))
                mapView = this
            }
        },
        update = { map ->
            // Re-centre on the start of the (possibly new) route
            if (routePoints.isNotEmpty()) {
                map.controller.setCenter(routePoints.first())
            }

            // Clear all overlays and redraw from scratch to reflect the latest routePoints
            map.overlays.clear()

            if (routePoints.size >= 2) {
                // Route polyline
                map.overlays.add(Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color       = android.graphics.Color.parseColor("#5A8F75")
                    outlinePaint.strokeWidth = 12f
                    outlinePaint.strokeCap   = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin  = android.graphics.Paint.Join.ROUND
                })

                // Start marker
                map.overlays.add(Marker(map).apply {
                    position = routePoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Start"
                })

                // End marker
                map.overlays.add(Marker(map).apply {
                    position = routePoints.last()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "End"
                })
            }

            map.invalidate()
        }
    )

    // Pause/resume the map tile downloader with the host Activity lifecycle
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView?.onPause()
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
