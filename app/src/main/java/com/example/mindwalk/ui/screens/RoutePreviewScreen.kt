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
import com.example.mindwalk.ui.viewmodel.PlanViewModel

@Composable
fun RoutePreviewScreen(
    vm: PlanViewModel,
    onBack: () -> Unit,
    onStartWalk: () -> Unit = {}
) {
    val routePoints = vm.previewRoutePoints

    Box(Modifier.fillMaxSize()) {
        OsmRouteMap(modifier = Modifier.fillMaxSize(), routePoints = routePoints)

        // Top bar overlay
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                    Text("← Back")
                }
                Text(
                    "Route Preview",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Bottom card
        RouteDetailsCard(
            distanceKm = 1.2,
            durationMin = 15,
            vibe = "Mindful",
            onStartWalk = onStartWalk,
            onTryAgain = { vm.generateABRoute() },
            onEdit = { onBack() },
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
                "Route Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("$distanceKm", "km\nDistance")
                StatItem("$durationMin min", "Duration")
                StatItem(vibe, "Vibe Score")
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
