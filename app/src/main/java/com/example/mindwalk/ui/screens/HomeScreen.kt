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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.CityPreferences
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*
import java.util.Calendar

/**
 * Data class holding the configuration for a preset walk card shown on [HomeScreen].
 *
 * @property label      Display name of the preset (e.g. "Touch the grass").
 * @property subtitle   Short description shown below the label (e.g. "10 min · Green").
 * @property icon       Material icon representing the preset visually.
 * @property iconTint   Colour applied to the icon.
 * @property mode       Mindfulness mode passed to [PlanYourWalkScreen] ("Green", "Quiet", "Sight").
 * @property durationMin Walk duration in minutes pre-filled in [PlanYourWalkScreen].
 */
private data class WalkPreset(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val mode: String,
    val durationMin: Int
)

private val PRESETS = listOf(
    WalkPreset("Touch the grass", "10 min · Green",  Icons.Default.Park,    Color(0xFF388E3C), "Green",  10),
    WalkPreset("City look",       "30 min · Sight",  Icons.Default.Place,   Color(0xFF1565C0), "Sight",  30),
    WalkPreset("Calm walk",       "20 min · Quiet",  Icons.Default.Spa,     Color(0xFF00897B), "Quiet",  20),
    WalkPreset("Mind reload",     "60 min · Green",  Icons.Default.Eco,     Color(0xFF558B2F), "Green",  60),
)

/**
 * Main home screen displayed after onboarding.
 *
 * Shows a time-sensitive greeting, the currently configured city with an inline
 * "Change city" button, a primary "Generate a walk" button that opens
 * [PlanYourWalkScreen] with default settings, and a 2×2 grid of [PresetCard]s for
 * one-tap access to four predefined walk configurations. A bottom navigation bar
 * provides access to [SavedRoutesScreen] and [JourneyScreen].
 *
 * @param onGenerateWalk Navigates to [PlanYourWalkScreen] with no pre-selection.
 * @param onQuickOption  Navigates to [PlanYourWalkScreen] with the preset [mode] and [duration] pre-filled.
 * @param onSaved        Switches the bottom nav tab to the Saved routes screen.
 * @param onJourney      Switches the bottom nav tab to the Journey screen.
 * @param onChangeCity   Navigates to the city-change screen.
 */
@Composable
fun HomeScreen(
    onGenerateWalk: () -> Unit,
    onQuickOption: (mode: String, duration: Int) -> Unit,
    onSaved: () -> Unit,
    onJourney: () -> Unit,
    onChangeCity: () -> Unit = {}
) {
    val context = LocalContext.current
    val city = remember { CityPreferences.getCity(context) ?: "" }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11  -> "Good morning"
            in 12..16 -> "Good afternoon"
            else      -> "Good evening"
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavBar(
                selected = BottomNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.SAVED   -> onSaved()
                        BottomNavTab.JOURNEY -> onJourney()
                        BottomNavTab.HOME    -> {}
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
                Spacer(Modifier.height(32.dp))

                // Greeting header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = greeting,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "What do you need today?",
                        fontSize = 16.sp,
                        color = Color(0xFF6B7280)
                    )
                    // City row — shows the configured city with a tap-to-change affordance
                    if (city.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onChangeCity
                                )
                                .padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Green600,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = city,
                                fontSize = 13.sp,
                                color = Green600,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Change city",
                                tint = Green600,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Generate walk button
                val genSource = remember { MutableInteractionSource() }
                val genPressed by genSource.collectIsPressedAsState()
                val genScale by animateFloatAsState(if (genPressed) 0.95f else 1f, label = "gen")

                Button(
                    onClick = onGenerateWalk,
                    interactionSource = genSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .scale(genScale),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green600),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "Generate a walk",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Preset walk cards — 2×2 grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PRESETS.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { preset ->
                                PresetCard(
                                    preset = preset,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onQuickOption(preset.mode, preset.durationMin) }
                                )
                            }
                        }
                    }
                }

                // Encouraging quote
                Text(
                    text = "\"Every walk is a small journey home to yourself\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: WalkPreset,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "preset_${preset.label}")

    Surface(
        color = Color.White.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                preset.icon,
                contentDescription = preset.label,
                tint = preset.iconTint,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = preset.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                textAlign = TextAlign.Center
            )
            Text(
                text = preset.subtitle,
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}
