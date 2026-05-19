package com.example.mindwalk.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mindwalk.ui.theme.Green600
import com.example.mindwalk.ui.theme.Red500
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.*

// ── Helpers ────────────────────────────────────────────────────────────────────

/**
 * Mindfulness prompts shown to the user every 30 seconds during the walk.
 * Each prompt encourages present-moment awareness.
 */
private val MINDFUL_PROMPTS = listOf(
    "Notice the sounds around you",
    "Feel your feet touching the ground",
    "Take a deep breath",
    "Notice the colors you see",
    "Feel the air on your skin"
)

private fun distMeters(a: GeoPoint, b: GeoPoint): Double {
    val R = 6_371_000.0
    val dLat = Math.toRadians(b.latitude  - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * R * asin(sqrt(h))
}

private fun bearing(from: GeoPoint, to: GeoPoint): Float {
    val dLon = Math.toRadians(to.longitude - from.longitude)
    val lat1 = Math.toRadians(from.latitude);  val lat2 = Math.toRadians(to.latitude)
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return ((Math.toDegrees(atan2(y, x)).toFloat() + 360f) % 360f)
}

private fun bearingText(deg: Float) = when {
    deg < 22.5f  || deg >= 337.5f -> "Head north"
    deg < 67.5f                   -> "Head northeast"
    deg < 112.5f                  -> "Head east"
    deg < 157.5f                  -> "Head southeast"
    deg < 202.5f                  -> "Head south"
    deg < 247.5f                  -> "Head southwest"
    deg < 292.5f                  -> "Head west"
    else                          -> "Head northwest"
}

private fun fmtDist(m: Double) =
    if (m < 1000) "${m.toInt()} m" else "%.1f km".format(m / 1000.0)

private fun fmtTime(s: Int) = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"

private fun closestIdx(route: List<GeoPoint>, user: GeoPoint): Int {
    var best = Double.MAX_VALUE; var idx = 0
    route.forEachIndexed { i, pt -> distMeters(pt, user).let { if (it < best) { best = it; idx = i } } }
    return idx
}

private fun remainingDist(route: List<GeoPoint>, from: Int): Double {
    var sum = 0.0
    for (i in from until route.size - 1) sum += distMeters(route[i], route[i + 1])
    return sum
}

private fun userDotBitmap(sizePx: Int): android.graphics.Bitmap {
    val bmp = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val c   = android.graphics.Canvas(bmp)
    val cx  = sizePx / 2f
    fun paint(color: Int, fill: Boolean = true) = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = if (fill) android.graphics.Paint.Style.FILL else android.graphics.Paint.Style.STROKE
    }
    c.drawCircle(cx, cx, cx,          paint(android.graphics.Color.argb(55, 33, 150, 243)))
    c.drawCircle(cx, cx, cx * 0.62f,  paint(android.graphics.Color.WHITE))
    c.drawCircle(cx, cx, cx * 0.50f,  paint(android.graphics.Color.parseColor("#2196F3")))
    return bmp
}

@SuppressLint("MissingPermission")
private fun startTracking(
    context: Context,
    lm: LocationManager,
    listener: LocationListener,
    onInit: (GeoPoint) -> Unit
) {
    // Register on GPS + Network so we get a fix even indoors / before GPS warms up
    runCatching {
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000L, 3f, listener)
    }
    runCatching {
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2_000L, 3f, listener)
    }
    // Seed with best last-known so map centers immediately while waiting for first fix
    (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
        ?.let { onInit(GeoPoint(it.latitude, it.longitude)) }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * Active walking screen shown while the user is following the generated route.
 *
 * Composed of three layers stacked in a [Box]:
 * 1. **Full-screen map** ([NavigatorMap]) — OSMDroid map that auto-follows the user's GPS position,
 *    draws the route polyline, and shows a custom blue user-position dot.
 * 2. **Direction banner** — shows a rotating compass arrow, the cardinal direction to the next
 *    waypoint (8 nodes lookahead), distance to that waypoint, and a GPS status icon.
 * 3. **Bottom panel** — elapsed time, walked distance (accumulated from GPS deltas via Haversine),
 *    remaining route distance, and the "End Walk" button.
 *
 * A coroutine increments [elapsedSecs] every second. Mindful prompts appear every 30 seconds
 * via [AnimatedVisibility] and can be dismissed by the user.
 *
 * GPS updates are registered via [LocationManager.requestLocationUpdates] on both GPS and Network
 * providers at 2-second / 3-metre intervals inside a [DisposableEffect] that removes the listener
 * when the composable leaves the composition.
 *
 * @param vm        Shared [PlanViewModel] providing the route coordinate list.
 * @param onEndWalk Navigates to [PostWalkReflectionScreen] when the user taps "End Walk".
 */
@Composable
fun WalkingScreen(vm: PlanViewModel, onEndWalk: () -> Unit) {
    val context     = LocalContext.current
    val routePoints = vm.previewRoutePoints

    var elapsedSecs  by remember { mutableStateOf(0) }
    var showPrompt   by remember { mutableStateOf(false) }
    var prompt       by remember { mutableStateOf(MINDFUL_PROMPTS.random()) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var walked       by remember { mutableStateOf(0.0) }
    var prevGeo      by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(Unit) { while (true) { delay(1_000L); elapsedSecs++ } }

    LaunchedEffect(Unit) {
        delay(30_000L)
        while (true) { prompt = MINDFUL_PROMPTS.random(); showPrompt = true; delay(30_000L) }
    }

    // GPS tracking
    DisposableEffect(Unit) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                val gp = GeoPoint(loc.latitude, loc.longitude)
                prevGeo?.let { walked += distMeters(it, gp) }
                prevGeo      = gp
                userLocation = gp
            }
            @Suppress("DEPRECATION")
            override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startTracking(context, lm, listener) { userLocation = it }
        }
        onDispose { lm.removeUpdates(listener) }
    }

    // Navigation maths
    val ci        = if (routePoints.isNotEmpty() && userLocation != null) closestIdx(routePoints, userLocation!!) else 0
    val lookahead = if (routePoints.isNotEmpty()) routePoints[minOf(ci + 8, routePoints.size - 1)] else null
    val hdg       = if (userLocation != null && lookahead != null) bearing(userLocation!!, lookahead) else 0f
    val distNext  = if (userLocation != null && lookahead != null) distMeters(userLocation!!, lookahead) else 0.0
    val remaining = if (routePoints.isNotEmpty()) remainingDist(routePoints, ci) else 0.0

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen navigator map
        NavigatorMap(
            modifier     = Modifier.fillMaxSize(),
            routePoints  = routePoints,
            userLocation = userLocation
        )

        // ── Direction banner ──────────────────────────────────────────────────
        Surface(
            modifier      = Modifier.align(Alignment.TopCenter).fillMaxWidth().statusBarsPadding(),
            color         = Color.White.copy(alpha = 0.96f),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier  = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rotating compass arrow
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(Green600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(28.dp).rotate(hdg)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (routePoints.isEmpty()) "No route loaded" else bearingText(hdg),
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1F2937)
                    )
                    Text(
                        if (routePoints.isEmpty()) "Generate a route first"
                        else "${fmtDist(distNext)} to next waypoint",
                        fontSize = 13.sp,
                        color    = Color(0xFF6B7280)
                    )
                }

                // GPS status indicator
                if (userLocation == null) {
                    Icon(Icons.Default.GpsNotFixed, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                } else {
                    Icon(Icons.Default.GpsFixed, null, tint = Green600, modifier = Modifier.size(22.dp))
                }
            }
        }

        // ── Mindful prompt ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = showPrompt,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp)
        ) {
            Surface(
                color           = Color.White.copy(alpha = 0.96f),
                shape           = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(prompt, fontSize = 16.sp, fontStyle = FontStyle.Italic,
                        color = Color(0xFF4B5563), textAlign = TextAlign.Center)
                    TextButton(onClick = { showPrompt = false }) {
                        Text("Dismiss", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }

        // ── Bottom panel: stats + stop ────────────────────────────────────────
        Surface(
            modifier        = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color           = Color.White.copy(alpha = 0.96f),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem("Elapsed",   fmtTime(elapsedSecs))
                    Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFFE2E8F0)))
                    StatItem("Walked",    fmtDist(walked))
                    Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFFE2E8F0)))
                    StatItem("Remaining", fmtDist(remaining))
                }

                // End walk button
                val stopSource  = remember { MutableInteractionSource() }
                val stopPressed by stopSource.collectIsPressedAsState()
                val stopScale   by animateFloatAsState(if (stopPressed) 0.95f else 1f, label = "stop")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(stopScale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Red500)
                        .clickable(interactionSource = stopSource, indication = null, onClick = onEndWalk)
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Stop, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("End Walk", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────────

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
        Text(label, fontSize  = 12.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun NavigatorMap(
    modifier: Modifier,
    routePoints: List<GeoPoint>,
    userLocation: GeoPoint?
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val userDotDrawable = remember {
        android.graphics.drawable.BitmapDrawable(context.resources, userDotBitmap(72))
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue   = "MindWalk/1.0 (Android; ${context.packageName})"
            osmdroidBasePath = context.cacheDir
            osmdroidTileCache = context.cacheDir
            load(context, context.getSharedPreferences("osmdroid", 0))
        }
    }

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
                controller.setZoom(18.0)
                controller.setCenter(
                    userLocation ?: routePoints.firstOrNull() ?: GeoPoint(49.1951, 16.6068)
                )
                mapView = this
            }
        },
        update = { map ->
            map.overlays.clear()

            // Route polyline + start/end markers
            if (routePoints.size >= 2) {
                map.overlays.add(Polyline().apply {
                    setPoints(routePoints)
                    outlinePaint.color       = android.graphics.Color.parseColor("#5A8F75")
                    outlinePaint.strokeWidth = 14f
                    outlinePaint.strokeCap   = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin  = android.graphics.Paint.Join.ROUND
                    outlinePaint.alpha       = 200
                })
                map.overlays.add(Marker(map).apply {
                    position = routePoints.first()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Start"
                })
                map.overlays.add(Marker(map).apply {
                    position = routePoints.last()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "End"
                })
            }

            // User position dot — auto-follow
            userLocation?.let { loc ->
                map.overlays.add(Marker(map).apply {
                    position = loc
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = userDotDrawable
                    setInfoWindow(null)
                })
                map.controller.animateTo(loc)
            }

            map.invalidate()
        }
    )

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
