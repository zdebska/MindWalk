package com.example.mindwalk.ui.screens

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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.theme.Green50
import com.example.mindwalk.ui.theme.Red500
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import kotlinx.coroutines.delay

private val MINDFUL_PROMPTS = listOf(
    "Notice the sounds around you",
    "Feel your feet touching the ground",
    "Take a deep breath",
    "Notice the colors you see",
    "Feel the air on your skin"
)

@Composable
fun WalkingScreen(
    vm: PlanViewModel,
    onEndWalk: () -> Unit
) {
    val routePoints = vm.previewRoutePoints

    var elapsedSeconds by remember { mutableStateOf(0) }
    var showPrompt by remember { mutableStateOf(true) }
    var currentPrompt by remember { mutableStateOf(MINDFUL_PROMPTS.random()) }

    // Increment timer every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            elapsedSeconds++
        }
    }

    // Rotate mindful prompts every 30 s
    LaunchedEffect(Unit) {
        delay(30_000L)
        while (true) {
            currentPrompt = MINDFUL_PROMPTS.random()
            showPrompt = true
            delay(30_000L)
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerText = "$minutes:${seconds.toString().padStart(2, '0')}"

    Box(modifier = Modifier.fillMaxSize().background(Green50)) {

        // Full-screen map
        OsmRouteMap(modifier = Modifier.fillMaxSize(), routePoints = routePoints)

        // Floating timer pill
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(50),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = timerText,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF1F2937)
                )
            }
        }

        // Mindful prompt overlay
        AnimatedVisibility(
            visible = showPrompt,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = currentPrompt,
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF4B5563),
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = { showPrompt = false }) {
                        Text("Dismiss", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }

        // End walk circular button
        val stopSource = remember { MutableInteractionSource() }
        val stopPressed by stopSource.collectIsPressedAsState()
        val stopScale by animateFloatAsState(if (stopPressed) 0.93f else 1f, label = "stop")

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(stopScale)
                    .clip(CircleShape)
                    .background(Red500)
                    .clickable(interactionSource = stopSource, indication = null, onClick = onEndWalk),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "End walk",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text("End walk", fontSize = 13.sp, color = Color.Gray)
        }
    }
}
