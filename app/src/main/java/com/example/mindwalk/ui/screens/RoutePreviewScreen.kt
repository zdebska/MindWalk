package com.example.mindwalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.theme.Green100
import com.example.mindwalk.ui.theme.Green600
import com.example.mindwalk.ui.theme.Green700
import com.example.mindwalk.ui.viewmodel.PlanViewModel

@Composable
fun RoutePreviewScreen(
    vm: PlanViewModel,
    onBack: () -> Unit,
    onStartWalk: () -> Unit = {}
) {
    val routePoints = vm.previewRoutePoints

    Box(Modifier.fillMaxSize()) {

        // ── Full-screen map ────────────────────────────────────────────────────
        OsmRouteMap(modifier = Modifier.fillMaxSize(), routePoints = routePoints)

        // ── Top header overlay ─────────────────────────────────────────────────
        Surface(
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
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
                        "Route Preview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        "Your mindful walk is ready",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        // ── Bottom details card ────────────────────────────────────────────────
        RouteDetailsCard(
            distanceKm  = vm.planDistanceKm,
            durationMin = vm.planDurationMin,
            vibe        = vm.planMode.ifEmpty { "Mindful" },
            onStartWalk = onStartWalk,
            onTryAgain  = { vm.generateABRoute() },
            onEdit      = { onBack() },
            modifier    = Modifier
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
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.97f),
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Text(
                "Route Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // Stats row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RouteStatItem(value = "${"%.1f".format(distanceKm)} km", label = "Distance")
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE2E8F0))
                RouteStatItem(value = "$durationMin min", label = "Duration")
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color(0xFFE2E8F0))
                RouteStatItem(value = vibe, label = "Vibe")
            }

            // Start Walk button
            Button(
                onClick = onStartWalk,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green600)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Start Walk",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Secondary actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onTryAgain,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green700),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New route", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(Icons.Default.Tune, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun RouteStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Green700
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}
