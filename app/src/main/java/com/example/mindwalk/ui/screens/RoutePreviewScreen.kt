package com.example.mindwalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RoutePreviewScreen(
    vm: PlanViewModel,
    onBack: () -> Unit
) {
    val routePoints = vm.previewRoutePoints

    Box(Modifier.fillMaxSize()) {
        OsmRouteMap(
            modifier = Modifier.fillMaxSize(),
            routePoints = routePoints
        )

        // Top bar overlay (simple like your mock)
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                TextButton(
                    onClick = onBack,
                    contentPadding = PaddingValues(0.dp)
                ) { Text("← Back") }

                Text(
                    text = "Route Preview",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Bottom card overlay
        RouteDetailsCard(
            distanceKm = 2.4,
            durationMin = 30,
            vibe = "Calm",
            onStartWalk = { /* TODO start walk */ },
            onTryAgain = {
                routePoints = fakeLoopRoute(
                    center = GeoPoint(49.1951, 16.6068),
                    radiusMeters = listOf(120.0, 160.0, 200.0).random(),
                    points = listOf(30, 40, 50).random()
                )
            },
            onEdit = { /* TODO open edit */ },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun RouteDetailsCard(
    distanceKm: Double,
    durationMin: Int,
    vibe: String,
    onStartWalk: () -> Unit,
    onTryAgain: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Route Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(title = "$distanceKm", subtitle = "km\nDistance")
                StatItem(title = "$durationMin min", subtitle = "Duration")
                StatItem(title = vibe, subtitle = "Vibe Score")
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onStartWalk,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A8F75))
            ) {
                Text("Start Walk", color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("↻  Try Again") }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("⚙  Edit") }
            }
        }
    }
}

@Composable
private fun StatItem(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

/**
 * Fake loop (circle-like) route around a center point.
 * Good enough for demo / UI while you build real routing.
 */
private fun fakeLoopRoute(center: GeoPoint, radiusMeters: Double, points: Int): List<GeoPoint> {
    val metersPerDegLat = 111_320.0
    val metersPerDegLon = 111_320.0 * cos(Math.toRadians(center.latitude))

    val dLat = radiusMeters / metersPerDegLat
    val dLon = radiusMeters / metersPerDegLon

    val out = ArrayList<GeoPoint>(points + 1)
    for (i in 0 until points) {
        val a = 2.0 * Math.PI * i / points
        val lat = center.latitude + dLat * sin(a)
        val lon = center.longitude + dLon * cos(a)
        out.add(GeoPoint(lat, lon))
    }
    out.add(out.first()) // close loop
    return out
}
