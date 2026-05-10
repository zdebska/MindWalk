package com.example.mindwalk.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*

private data class SavedRouteItem(
    val name: String,
    val duration: Int,
    val distanceKm: Double,
    val feel: String,
    val tags: List<String>,
    val saves: Int
)

@Composable
fun SavedRoutesScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onJourney: () -> Unit
) {
    val routes = remember {
        listOf(
            SavedRouteItem(
                "Quiet park loop", 15, 1.2, "calm",
                listOf("Worked well when stressed", "Peaceful", "Lots of trees"), 12
            ),
            SavedRouteItem(
                "Canal morning walk", 25, 2.1, "green",
                listOf("Great for clearing mind", "Lots of trees"), 8
            ),
            SavedRouteItem(
                "City center loop", 20, 1.8, "balanced",
                listOf("Good short break walk"), 5
            )
        )
    }

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

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                    }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text("Saved routes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Your favorite peaceful walks", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }

                if (routes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Eco, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(64.dp))
                        Text("No saved routes yet", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text(
                            "Save routes after your walks to find them here",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    routes.forEach { route -> SavedRouteCard(route) }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SavedRouteCard(route: SavedRouteItem) {
    val source = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f, label = "card_${route.name}")

    Surface(
        color = if (isPressed) Color.White else Color.White.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = source, indication = null) {}
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Name + save count
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(route.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Red400, modifier = Modifier.size(16.dp))
                    Text("${route.saves}", fontSize = 13.sp, color = Color.Gray)
                }
            }

            // Duration + distance
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule,   null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text("${route.duration} min",  fontSize = 13.sp, color = Color(0xFF6B7280))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text("${route.distanceKm} km", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }

            // Feel badge
            val (badgeBg, badgeText) = when (route.feel) {
                "calm"  -> Pair(Blue100,         Blue700)
                "green" -> Pair(Green100,        Green700)
                else    -> Pair(Color(0xFFF3F4F6), Color(0xFF374151))
            }
            Surface(color = badgeBg, shape = RoundedCornerShape(50)) {
                Text(
                    route.feel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = badgeText
                )
            }

            // Tags — wrapped in chunked rows to avoid experimental FlowRow
            route.tags.chunked(2).forEach { rowTags ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTags.forEach { tag ->
                        Surface(color = Green50, shape = RoundedCornerShape(50)) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Green700
                            )
                        }
                    }
                }
            }
        }
    }
}
