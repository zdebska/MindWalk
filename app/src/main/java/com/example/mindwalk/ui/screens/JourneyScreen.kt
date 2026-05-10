package com.example.mindwalk.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*

private data class PlantConfig(val icon: ImageVector, val size: Dp, val color: Color, val delayMs: Int)
private data class Moment(val name: String, val date: String, val emoji: String, val feeling: String)

@Composable
fun JourneyScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSaved: () -> Unit
) {
    val plants = remember {
        listOf(
            PlantConfig(Icons.Default.Eco,          20.dp, Green400,  0),
            PlantConfig(Icons.Default.LocalFlorist,  24.dp, Pink500,  100),
            PlantConfig(Icons.Default.Eco,           20.dp, Green600, 200),
            PlantConfig(Icons.Default.LocalFlorist,  28.dp, Purple500,300),
            PlantConfig(Icons.Default.Park,          24.dp, Green500, 400),
            PlantConfig(Icons.Default.Eco,           32.dp, Green700, 500),
            PlantConfig(Icons.Default.LocalFlorist,  20.dp, Yellow500,600),
            PlantConfig(Icons.Default.Park,          28.dp, Green400, 700)
        )
    }

    val moments = remember {
        listOf(
            Moment("Morning park loop",  "Today, 8:15 AM",      "🌿", "refreshed"),
            Moment("Canal side walk",    "Yesterday, 6:30 PM",  "🌸", "calm"),
            Moment("Forest trail",       "Mon, Apr 28",         "✨", "energized")
        )
    }

    var plantsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { plantsVisible = true }

    // One animation state per plant (correct Compose pattern)
    val pa0 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 0),   label = "pa0")
    val pa1 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 100), label = "pa1")
    val pa2 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 200), label = "pa2")
    val pa3 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 300), label = "pa3")
    val pa4 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 400), label = "pa4")
    val pa5 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 500), label = "pa5")
    val pa6 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 600), label = "pa6")
    val pa7 by animateFloatAsState(if (plantsVisible) 1f else 0f, tween(500, 700), label = "pa7")
    val alphas = listOf(pa0, pa1, pa2, pa3, pa4, pa5, pa6, pa7)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(
                selected = BottomNavTab.JOURNEY,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME  -> onHome()
                        BottomNavTab.SAVED -> onSaved()
                        else               -> {}
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                    }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text("Your journey", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Watch your garden grow with each walk", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }

                // Garden card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.verticalGradient(listOf(Green50, Blue50)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Row 1: plants 0–3
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            plants.take(4).forEachIndexed { i, p ->
                                Icon(p.icon, null, tint = p.color,
                                    modifier = Modifier.size(p.size).alpha(alphas[i]))
                            }
                        }
                        // Row 2: plants 4–7
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            plants.drop(4).forEachIndexed { i, p ->
                                Icon(p.icon, null, tint = p.color,
                                    modifier = Modifier.size(p.size).alpha(alphas[i + 4]))
                            }
                        }

                        Text(
                            "Keep walking to grow more plants",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }

                // Stats row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    JourneyStatCard("8",    "Total walks", Modifier.weight(1f))
                    JourneyStatCard("3",    "This month",  Modifier.weight(1f))
                    JourneyStatCard("Calm", "Favorite",    Modifier.weight(1f))
                }

                // Recent moments
                Text("Recent moments", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))

                moments.forEach { moment ->
                    Surface(
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(moment.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text(moment.date, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                                Text("Felt ${moment.feeling}", fontSize = 13.sp, color = Color(0xFF6B7280))
                            }
                            Text(moment.emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun JourneyStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Text(label, fontSize = 12.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
        }
    }
}
