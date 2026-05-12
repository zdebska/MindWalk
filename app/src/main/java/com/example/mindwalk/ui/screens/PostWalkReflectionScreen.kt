package com.example.mindwalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.SavedRoute
import com.example.mindwalk.data.SerializablePoint
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import com.example.mindwalk.ui.viewmodel.SavedRoutesViewModel
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun PostWalkReflectionScreen(
    planVm: PlanViewModel,
    savedVm: SavedRoutesViewModel,
    onComplete: () -> Unit
) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var showReward   by remember { mutableStateOf(false) }
    var wantToSave   by remember { mutableStateOf(false) }
    var routeName    by remember {
        mutableStateOf("${planVm.planMode} ${if (planVm.planShape == "Line") "Walk" else "Loop"}")
    }

    if (showReward) {
        RewardScreen(onComplete = onComplete)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Green50, Blue50)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "How did this walk feel?",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)
                )
                Text(
                    "Your reflections help us personalize future walks",
                    fontSize = 14.sp, color = Color(0xFF6B7280)
                )
            }

            // Mood 2×2 grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MoodButton("Great",    Icons.Default.SentimentVerySatisfied, Green600,          selectedMood == "Great",    Modifier.weight(1f)) { selectedMood = "Great" }
                    MoodButton("Good",     Icons.Default.SentimentSatisfied,     Blue600,           selectedMood == "Good",     Modifier.weight(1f)) { selectedMood = "Good" }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MoodButton("Okay",     Icons.Default.SentimentNeutral,       Color(0xFFD97706), selectedMood == "Okay",     Modifier.weight(1f)) { selectedMood = "Okay" }
                    MoodButton("Not great",Icons.Default.SentimentDissatisfied,  Color.Gray,        selectedMood == "Not great",Modifier.weight(1f)) { selectedMood = "Not great" }
                }
            }

            // ── Save route card ───────────────────────────────────────────────
            Surface(
                color = Blue50,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite, null,
                            tint = Blue600, modifier = Modifier.size(24.dp)
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Save this route?", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                            Text(
                                "Add it to your collection for next time",
                                fontSize = 12.sp, color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = wantToSave,
                            onCheckedChange = { wantToSave = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor  = Green600,
                                checkedTrackColor  = Green100
                            )
                        )
                    }

                    AnimatedVisibility(visible = wantToSave) {
                        OutlinedTextField(
                            value         = routeName,
                            onValueChange = { routeName = it },
                            label         = { Text("Route name") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Green600,
                                focusedLabelColor    = Green600
                            )
                        )
                    }
                }
            }

            // Complete button — appears after mood selection
            AnimatedVisibility(
                visible = selectedMood != null,
                enter   = fadeIn() + slideInVertically { it / 2 }
            ) {
                Button(
                    onClick = {
                        if (wantToSave && routeName.isNotBlank()) {
                            savedVm.save(
                                SavedRoute(
                                    id          = UUID.randomUUID().toString(),
                                    name        = routeName.trim(),
                                    savedAt     = System.currentTimeMillis(),
                                    durationMin = planVm.planDurationMin,
                                    distanceKm  = planVm.planDistanceKm,
                                    mode        = planVm.planMode,
                                    shape       = planVm.planShape,
                                    mood        = selectedMood ?: "",
                                    startName   = planVm.startLocationName,
                                    points      = planVm.previewRoutePoints.map {
                                        SerializablePoint(it.latitude, it.longitude)
                                    }
                                )
                            )
                        }
                        showReward = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Green600)
                ) {
                    Text("Complete walk", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoodButton(
    mood: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "mood_$mood")

    Surface(
        color           = if (isSelected) Green600 else Color.White.copy(alpha = 0.7f),
        shape           = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 8.dp else 0.dp,
        modifier        = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon, mood,
                tint     = if (isSelected) Color.White else iconColor,
                modifier = Modifier.size(40.dp)
            )
            Text(
                mood,
                color      = if (isSelected) Color.White else Color(0xFF374151),
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp,
                textAlign  = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RewardScreen(onComplete: () -> Unit) {
    LaunchedEffect(Unit) { delay(2_000L); onComplete() }

    val inf = rememberInfiniteTransition(label = "reward")
    val bounce by inf.animateFloat(1f, 1.12f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bounce")
    val sp1 by inf.animateFloat(0.4f, 1f,   infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "sp1")
    val sp2 by inf.animateFloat(0.9f, 0.3f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "sp2")
    val sp3 by inf.animateFloat(1f,   0.5f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "sp3")

    Box(
        modifier        = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Green50, Blue50))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier         = Modifier.size(160.dp).scale(bounce).background(Green100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, null, tint = Green600, modifier = Modifier.size(80.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("You earned a leaf!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Green700)
                Text("Another step on your journey", fontSize = 16.sp, color = Color(0xFF6B7280))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = Yellow500, modifier = Modifier.size(28.dp).alpha(sp1))
                Icon(Icons.Default.AutoAwesome, null, tint = Yellow500, modifier = Modifier.size(28.dp).alpha(sp2))
                Icon(Icons.Default.AutoAwesome, null, tint = Yellow500, modifier = Modifier.size(28.dp).alpha(sp3))
            }
        }
    }
}
