package com.example.mindwalk.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.SavedRoute
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.SavedRoutesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedRoutesScreen(
    savedVm: SavedRoutesViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onJourney: () -> Unit,
    onRouteSelected: (SavedRoute) -> Unit = {}
) {
    LaunchedEffect(Unit) { savedVm.reload() }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(
                selected = BottomNavTab.SAVED,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME    -> onHome()
                        BottomNavTab.JOURNEY -> onJourney()
                        else                 -> {}
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Green50, Blue50)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                    }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text("Saved routes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(
                            if (savedVm.routes.isEmpty()) "Your favorite peaceful walks"
                            else "${savedVm.routes.size} saved walk${if (savedVm.routes.size != 1) "s" else ""}",
                            fontSize = 14.sp, color = Color(0xFF6B7280)
                        )
                    }
                }

                if (savedVm.routes.isEmpty()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Eco, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                        Text("No saved routes yet", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text(
                            "After a walk, toggle \"Save this route\" to find it here",
                            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center
                        )
                    }
                } else {
                    savedVm.routes.forEach { route ->
                        SavedRouteCard(
                            route    = route,
                            onDelete = { savedVm.delete(route.id) },
                            onClick  = { onRouteSelected(route) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SavedRouteCard(route: SavedRoute, onDelete: () -> Unit, onClick: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateStr = remember(route.savedAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(route.savedAt))
    }

    Surface(
        color           = Color.White.copy(alpha = 0.9f),
        shape           = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        onClick         = onClick,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Name + delete button
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(route.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF1F2937))
                    Text(dateStr, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
                IconButton(
                    onClick  = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, "Delete", tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
                }
            }

            // Duration + distance + start
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip(Icons.Default.Schedule,   "${route.durationMin} min")
                StatChip(Icons.Default.LocationOn, "${"%.1f".format(route.distanceKm)} km")
                if (route.startName.isNotEmpty() && route.startName != "Select end point") {
                    StatChip(Icons.Default.Place, route.startName.take(16) + if (route.startName.length > 16) "…" else "")
                }
            }

            // Mode + mood badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (modeBg, modeFg) = when (route.mode) {
                    "Calm"    -> Pair(Blue100,          Blue700)
                    "Scenic"  -> Pair(Green100,         Green700)
                    "Explore" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                    else      -> Pair(Color(0xFFF3F4F6), Color(0xFF374151))
                }
                Badge(containerColor = modeBg, contentColor = modeFg, text = route.mode)
                Badge(containerColor = Green50, contentColor = Green700, text = route.shape)
                if (route.mood.isNotEmpty()) {
                    val moodColor = when (route.mood) {
                        "Great"     -> Green600
                        "Good"      -> Blue600
                        "Okay"      -> Color(0xFFD97706)
                        else        -> Color.Gray
                    }
                    Badge(containerColor = moodColor.copy(alpha = 0.12f), contentColor = moodColor, text = route.mood)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title            = { Text("Delete route?") },
            text             = { Text("\"${route.name}\" will be removed from your saved routes.") },
            confirmButton    = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
        Text(text, fontSize = 13.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun Badge(containerColor: Color, contentColor: Color, text: String) {
    Surface(color = containerColor, shape = RoundedCornerShape(50)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize  = 12.sp,
            color     = contentColor
        )
    }
}
