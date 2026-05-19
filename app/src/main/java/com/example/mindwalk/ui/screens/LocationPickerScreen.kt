package com.example.mindwalk.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mindwalk.ui.theme.Green600
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Returns the most recent known location from GPS or Network provider, or null if unavailable.
 *
 * Used as the fast path before requesting a live location fix.
 *
 * @param lm System [LocationManager] instance.
 * @return Last known [GeoPoint], or null if neither provider has a cached location.
 */
@SuppressLint("MissingPermission")
private fun lastKnownGeoPoint(lm: LocationManager): GeoPoint? =
    (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
        ?.let { GeoPoint(it.latitude, it.longitude) }

/**
 * Registers a one-shot location listener on GPS and Network providers.
 *
 * The listener removes itself from [lm] after the first fix is delivered to [onLocation],
 * so no manual cleanup is required in the success path. The returned [LocationListener]
 * should be passed to [LocationManager.removeUpdates] in the error or cancellation path.
 *
 * @param lm         System [LocationManager] instance.
 * @param onLocation Callback invoked with the resolved [GeoPoint] on first fix.
 * @return The registered [LocationListener] for optional manual removal.
 */
@SuppressLint("MissingPermission")
private fun requestOneShotLocation(
    lm: LocationManager,
    onLocation: (GeoPoint) -> Unit
): LocationListener {
    val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            lm.removeUpdates(this)
            onLocation(GeoPoint(loc.latitude, loc.longitude))
        }
        @Suppress("DEPRECATION")
        override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
    }
    runCatching {
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
    }
    runCatching {
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener)
    }
    return listener
}

/**
 * Full-screen map composable that lets the user pick a geographic point by tapping.
 *
 * On first open, the map attempts to centre on the device's current location:
 * - Requests [android.Manifest.permission.ACCESS_FINE_LOCATION] if not already granted.
 * - Uses the last known cached location for an instant result, falling back to a live one-shot fix.
 *
 * The user can also tap anywhere on the map to place or move the selection marker. A "My Location"
 * FAB (floating action button) re-centres the selection on the device's current position at any time.
 *
 * Tapping "Confirm Location" stores the selected [GeoPoint] and label in [PlanViewModel] via
 * [PlanViewModel.setStartLocation] or [PlanViewModel.setEndLocation], then pops the back stack.
 *
 * @param vm      Shared [PlanViewModel] that receives the confirmed location.
 * @param isStart True to set the start location; false to set the end location.
 * @param onBack  Navigation callback invoked after confirmation or on the Back button.
 */
@Composable
fun LocationPickerScreen(
    vm: PlanViewModel,
    isStart: Boolean = true,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val initialPoint = if (isStart) vm.startLocation else vm.endLocation
    val initialName  = if (isStart) vm.startLocationName else vm.endLocationName

    var selectedPoint  by remember { mutableStateOf(initialPoint ?: GeoPoint(49.1951, 16.6068)) }
    var selectedName   by remember { mutableStateOf(initialName) }

    fun hasPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Drives the DisposableEffect below — flips to true once permission is known to be granted.
    var permissionGranted by remember { mutableStateOf(hasPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> permissionGranted = isGranted }

    // Ask for permission on first open (if not already granted and no point pre-set).
    LaunchedEffect(Unit) {
        if (initialPoint == null && !hasPermission()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // When permission is available and no point is pre-set: get current location.
    // Tries last-known first (instant); falls back to a live one-shot request.
    DisposableEffect(permissionGranted) {
        if (!permissionGranted || initialPoint != null) return@DisposableEffect onDispose {}

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val cached = lastKnownGeoPoint(lm)
        if (cached != null) {
            selectedPoint = cached
            selectedName  = "Current Location"
            return@DisposableEffect onDispose {}
        }

        val listener = requestOneShotLocation(lm) { gp ->
            selectedPoint = gp
            selectedName  = "Current Location"
        }
        onDispose { lm.removeUpdates(listener) }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen map ────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(selectedPoint)

                    val marker = Marker(this).apply {
                        position = selectedPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Location"
                    }
                    overlays.add(marker)

                    overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let {
                                selectedPoint   = it
                                selectedName    = "Pinned Location"
                                marker.position = it
                                invalidate()
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?) = false
                    }))
                }
            },
            update = { map ->
                map.controller.setCenter(selectedPoint)
                map.overlays.filterIsInstance<Marker>().firstOrNull()?.position = selectedPoint
                map.invalidate()
            }
        )

        // ── Top header ─────────────────────────────────────────────────────────
        Surface(
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                }
                Column(Modifier.padding(start = 4.dp)) {
                    Text(
                        if (isStart) "Select Start Location" else "Select End Location",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)
                    )
                    Text("Tap on the map to pick a point", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
        }

        // ── My Location FAB ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(Green600),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {
                if (!hasPermission()) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val cached = lastKnownGeoPoint(lm)
                    if (cached != null) {
                        selectedPoint = cached
                        selectedName  = "Current Location"
                    } else {
                        // Fire-and-forget one-shot: listener removes itself after first fix
                        requestOneShotLocation(lm) { gp ->
                            selectedPoint = gp
                            selectedName  = "Current Location"
                        }
                    }
                }
            }) {
                Icon(Icons.Default.MyLocation, "My Location", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        // ── Confirm button ─────────────────────────────────────────────────────
        Surface(
            color    = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (isStart) vm.setStartLocation(selectedPoint, selectedName)
                    else         vm.setEndLocation(selectedPoint, selectedName)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Icon(Icons.Default.MyLocation, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Location", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
