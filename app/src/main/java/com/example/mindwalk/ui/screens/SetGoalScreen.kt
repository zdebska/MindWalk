package com.example.mindwalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.MonthlyGoal
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.WalkHistoryViewModel
import kotlin.math.ceil

/**
 * Configuration for a single selectable goal metric.
 *
 * @property key      Identifier stored in [com.example.mindwalk.data.MonthlyGoal.type]
 *                    (`"walks"`, `"distance"`, or `"time"`).
 * @property label    Display name shown on the type card.
 * @property icon     Material icon representing the metric.
 * @property rangeMin Minimum slider value (inclusive).
 * @property rangeMax Maximum slider value (inclusive).
 * @property steps    Number of discrete snap points between [rangeMin] and [rangeMax], exclusive
 *                    of the endpoints — passed directly to [Slider].
 * @property default  Initial slider position when no saved goal exists for this type.
 * @property unit     Short unit label used in [targetLabel] and [contextHint].
 */
private data class GoalType(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val rangeMin: Float,
    val rangeMax: Float,
    val steps: Int,
    val default: Int,
    val unit: String
)

/**
 * The three available goal metrics, each fully configured for slider range and defaults.
 *
 * - Walks:    4–30, step 1, default 12
 * - Distance: 5–100 km, step 5, default 20
 * - Time:     60–600 min, step 30, default 180
 */
private val GOAL_TYPES = listOf(
    GoalType("walks",    "Walks",    Icons.AutoMirrored.Filled.DirectionsWalk, 4f,  30f,  25, 12,  "walks"),
    GoalType("distance", "Distance", Icons.Default.LocationOn,                 5f,  100f, 18, 20,  "km"),
    GoalType("time",     "Time",     Icons.Default.Schedule,                   60f, 600f, 17, 180, "min"),
)

/**
 * Screen for creating or updating the user's monthly walking goal.
 *
 * Lets the user pick a goal type (walks, distance, time) and set a target using a slider.
 * If a goal already exists it is pre-selected and the slider is pre-filled. The confirm
 * button reads "Save goal" on first creation and "Update goal" on edit.
 *
 * On save, [WalkHistoryViewModel.saveGoal] persists the goal and [onBack] is called to
 * return to [JourneyScreen] which will immediately display the new progress card.
 *
 * @param walkHistoryVm Provides the existing goal (if any) and the [WalkHistoryViewModel.saveGoal] method.
 * @param onBack        Navigates back to [JourneyScreen].
 */
@Composable
fun SetGoalScreen(
    walkHistoryVm: WalkHistoryViewModel,
    onBack: () -> Unit
) {
    val existing = walkHistoryVm.monthlyGoal

    var selectedType by remember {
        mutableStateOf(existing?.let { g -> GOAL_TYPES.find { it.key == g.type } } ?: GOAL_TYPES[0])
    }
    var sliderValue by remember {
        mutableStateOf(
            existing?.target?.toFloat()
                ?: selectedType.default.toFloat()
        )
    }

    // When the type card changes, reset slider to saved value for that type or its default
    LaunchedEffect(selectedType) {
        sliderValue = if (existing?.type == selectedType.key) {
            existing.target.toFloat()
        } else {
            selectedType.default.toFloat()
        }
    }

    val target = sliderValue.toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Green50, Blue50)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 96.dp),
        ) {
            // Header
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                }
                Column(Modifier.padding(start = 4.dp)) {
                    Text(
                        if (existing != null) "Update your goal" else "Set your goal",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)
                    )
                    Text(
                        "Track your monthly walking progress",
                        fontSize = 14.sp, color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Goal type cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GOAL_TYPES.forEach { type ->
                    GoalTypeCard(
                        type = type,
                        selected = selectedType.key == type.key,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = type }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Slider + context hint
            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target", fontSize = 14.sp, color = Color(0xFF6B7280))
                        Text(
                            targetLabel(selectedType, target),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green600
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = selectedType.rangeMin..selectedType.rangeMax,
                        steps = selectedType.steps,
                        colors = SliderDefaults.colors(
                            thumbColor = Green600,
                            activeTrackColor = Green600,
                            inactiveTrackColor = Green100
                        )
                    )

                    Text(
                        contextHint(selectedType, target),
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save / Update button pinned to bottom
        Button(
            onClick = {
                walkHistoryVm.saveGoal(MonthlyGoal(selectedType.key, target))
                onBack()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Text(
                if (existing != null) "Update goal" else "Save goal",
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }
    }
}

/**
 * Tappable card representing one goal metric option.
 *
 * Turns green when [selected]; otherwise shows a white semi-transparent background.
 *
 * @param type     The [GoalType] this card represents.
 * @param selected Whether this card is the currently active selection.
 * @param modifier Layout modifier applied to the card surface.
 * @param onClick  Called when the card is tapped to select [type].
 */
@Composable
private fun GoalTypeCard(
    type: GoalType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Green600 else Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (selected) 6.dp else 1.dp,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                type.icon,
                contentDescription = type.label,
                tint = if (selected) Color.White else Green600,
                modifier = Modifier.size(24.dp)
            )
            Text(
                type.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF374151),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Returns the formatted target string shown next to the slider (e.g. "20 km", "12 walks").
 *
 * @param type   The currently selected [GoalType].
 * @param target The current slider value as an integer.
 */
private fun targetLabel(type: GoalType, target: Int): String = when (type.key) {
    "distance" -> "$target km"
    "time"     -> "$target min"
    else       -> "$target walks"
}

/**
 * Returns the context hint shown below the slider to give the target a real-world frame
 * (e.g. "That's about 3 walks per week", "Around 3.0 hours of mindful walking").
 *
 * @param type   The currently selected [GoalType].
 * @param target The current slider value as an integer.
 */
private fun contextHint(type: GoalType, target: Int): String = when (type.key) {
    "walks"    -> "That's about ${ceil(target / 4.0).toInt()} walks per week"
    "distance" -> "About ${"%.1f".format(target / 4.0)} km per week"
    "time"     -> "Around ${"%.1f".format(target / 60.0)} hours of mindful walking"
    else       -> ""
}
