package com.example.mindwalk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mindwalk.ui.theme.Green600
import com.example.mindwalk.ui.theme.Green700
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun LocationPickerScreen(
    vm: PlanViewModel,
    isStart: Boolean = true,
    onBack: () -> Unit
) {
    val initialPoint = if (isStart) vm.startLocation else vm.endLocation
    val initialName  = if (isStart) vm.startLocationName else vm.endLocationName

    var searchQuery by remember { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf(initialPoint ?: GeoPoint(49.1951, 16.6068)) }
    var selectedName  by remember { mutableStateOf(initialName) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full-screen map ────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(selectedPoint)

                    val marker = Marker(this).apply {
                        position = selectedPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Location"
                    }
                    overlays.add(marker)

                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let {
                                selectedPoint = it
                                selectedName  = "Pinned Location"
                                marker.position = it
                                invalidate()
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?) = false
                    }
                    overlays.add(MapEventsOverlay(receiver))
                }
            },
            update = { map ->
                map.controller.setCenter(selectedPoint)
                map.overlays.filterIsInstance<Marker>().firstOrNull()?.position = selectedPoint
                map.invalidate()
            }
        )

        // ── Top header overlay ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF374151)
                        )
                    }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text(
                            if (isStart) "Select Start Location" else "Select End Location",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            "Tap on the map to pick a point",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Search bar
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search location…", color = Color(0xFF9CA3AF)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
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
            IconButton(
                onClick = {
                    selectedPoint = GeoPoint(49.1911, 16.6122)
                    selectedName  = "Current Location"
                }
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Confirm button ─────────────────────────────────────────────────────
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (isStart) vm.setStartLocation(selectedPoint, selectedName)
                    else vm.setEndLocation(selectedPoint, selectedName)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Confirm Location",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
