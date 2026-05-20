package com.example.mindwalk.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.MonthlyGoal
import com.example.mindwalk.data.WalkRecord
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.WalkHistoryViewModel

private val leafColors = listOf(
    Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF66BB6A),
    Color(0xFF00897B), Color(0xFF004D40), Color(0xFF558B2F),
    Color(0xFF9CCC65), Color(0xFFFFB300), Color(0xFFF57C00),
    Color(0xFFE64A19), Color(0xFF827717), Color(0xFFF9A825),
)
private val leafRotations = listOf(-15f, 8f, -5f, 20f, -12f, 5f, -18f, 10f, -3f, 15f, -8f, 22f)
private val leafSizeDps   = listOf(28, 24, 32, 26, 30, 22, 28, 34, 24, 28, 26, 30)

/**
 * Journey screen displaying the user's walking history, monthly goal progress, and leaf collection.
 *
 * @param walkHistoryVm Provides walk records, month progress stats, and the active [MonthlyGoal].
 * @param onBack        Navigates back (pops the back stack).
 * @param onHome        Switches the bottom nav tab to [HomeScreen].
 * @param onSaved       Switches the bottom nav tab to [SavedRoutesScreen].
 * @param onSetGoal     Navigates to [SetGoalScreen] from both the dashed "Set goal" button
 *                      and the "Change" link on the active goal card.
 */
@Composable
fun JourneyScreen(
    walkHistoryVm: WalkHistoryViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSaved: () -> Unit,
    onSetGoal: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        walkHistoryVm.reload()
        walkHistoryVm.reloadGoal()
    }

    val walks        = walkHistoryVm.walks
    val goal         = walkHistoryVm.monthlyGoal
    val totalWalks   = walks.size
    val popularMode  = walkHistoryVm.popularMode

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
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF374151))
                    }
                    Column(Modifier.padding(start = 4.dp)) {
                        Text("Your journey", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Every walk earns a leaf", fontSize = 14.sp, color = Color(0xFF6B7280))
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JourneyStatCard(totalWalks.toString(),                        "Total walks", Modifier.weight(1f))
                    JourneyStatCard(walkHistoryVm.walksThisMonth.toString(),      "This month",  Modifier.weight(1f))
                    JourneyStatCard(popularMode,                                  "Top mode",    Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // Goal section
                GoalSection(
                    goal            = goal,
                    walksThisMonth  = walkHistoryVm.walksThisMonth,
                    distanceThisMonth = walkHistoryVm.distanceThisMonthKm,
                    timeThisMonth   = walkHistoryVm.timeThisMonthMin,
                    onSetGoal       = onSetGoal,
                    modifier        = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Leaf board fills remaining space
                LeafBoard(walks, modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Goal section ──────────────────────────────────────────────────────────────

/**
 * Adaptive goal card placed between the stats row and the leaf board.
 *
 * **State 1 — no goal set:** renders a full-width dashed-border button that opens
 * [SetGoalScreen] via [onSetGoal].
 *
 * **State 2 — goal active:** renders a gradient card with the goal type icon, a
 * "current / target" label, an animated [LinearProgressIndicator]-style bar that fills
 * from [Green500] to [Green600], and a "X% complete" caption. A "Change" link also
 * opens [SetGoalScreen].
 *
 * Progress is capped at 100% and animated with a 600 ms tween on first composition.
 *
 * Note: [distanceThisMonth] and [timeThisMonth] reflect **planned** route values stored
 * in [com.example.mindwalk.data.WalkRecord], not GPS-tracked activity.
 *
 * @param goal              The user's saved [MonthlyGoal], or null if none exists.
 * @param walksThisMonth    Number of walks completed in the current calendar month.
 * @param distanceThisMonth Total planned route distance this month in kilometres.
 * @param timeThisMonth     Total planned walk duration this month in minutes.
 * @param onSetGoal         Navigates to [SetGoalScreen].
 * @param modifier          Layout modifier applied to the outermost container.
 */
@Composable
private fun GoalSection(
    goal: MonthlyGoal?,
    walksThisMonth: Int,
    distanceThisMonth: Double,
    timeThisMonth: Int,
    onSetGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (goal == null) {
        // State 1: no goal — dashed "Set your monthly goal" button
        val dashedColor = Green600.copy(alpha = 0.5f)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = dashedColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSetGoal
                )
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Flag, null, tint = Green700, modifier = Modifier.size(20.dp))
                Text("Set your monthly goal", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Green700)
            }
        }
    } else {
        // State 2: goal active — progress card
        val currentValue = when (goal.type) {
            "distance" -> distanceThisMonth.toFloat()
            "time"     -> timeThisMonth.toFloat()
            else       -> walksThisMonth.toFloat()
        }
        val progress = (currentValue / goal.target.toFloat()).coerceIn(0f, 1f)
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(600),
            label = "goal_progress"
        )

        Surface(
            color = Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(Green50, Blue50)))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                goalIcon(goal.type), null,
                                tint = Green700,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    goalLabel(goal.type),
                                    fontSize = 12.sp, color = Color(0xFF6B7280)
                                )
                                Text(
                                    progressText(goal, walksThisMonth, distanceThisMonth, timeThisMonth),
                                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)
                                )
                            }
                        }
                        Text(
                            "Change",
                            fontSize = 13.sp,
                            color = Green700,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSetGoal
                            )
                        )
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(Green500, Green600)))
                        )
                    }

                    // Percentage
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

/** Maps a goal type key to its representative Material icon. */
private fun goalIcon(type: String): ImageVector = when (type) {
    "distance" -> Icons.Default.LocationOn
    "time"     -> Icons.Default.Schedule
    else       -> Icons.AutoMirrored.Filled.DirectionsWalk
}

/** Returns the small label text shown above the progress value (e.g. "Monthly walks goal"). */
private fun goalLabel(type: String): String = when (type) {
    "distance" -> "Monthly distance goal"
    "time"     -> "Monthly time goal"
    else       -> "Monthly walks goal"
}

/**
 * Formats the "current / target unit" string shown in bold on the goal card
 * (e.g. `"3 / 12 walks"`, `"3.2 / 20 km"`, `"45 / 180 min"`).
 *
 * @param goal     The active [MonthlyGoal] providing the target and type.
 * @param walks    Walks completed this month.
 * @param distance Distance walked this month in km.
 * @param time     Time walked this month in minutes.
 */
private fun progressText(
    goal: MonthlyGoal,
    walks: Int,
    distance: Double,
    time: Int
): String = when (goal.type) {
    "distance" -> "${"%.1f".format(distance)} / ${goal.target} km"
    "time"     -> "$time / ${goal.target} min"
    else       -> "$walks / ${goal.target} walks"
}

// ── Leaf board ────────────────────────────────────────────────────────────────

@Composable
private fun LeafBoard(walks: List<WalkRecord>, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFF5F0E8),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Leaf collection", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
                Text(
                    if (walks.isEmpty()) "No leaves yet"
                    else "${walks.size} leaf${if (walks.size != 1) "s" else ""}",
                    fontSize = 13.sp, color = Color(0xFF6B7280)
                )
            }

            if (walks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Eco, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(56.dp))
                    Text("Your board is empty", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF9CA3AF))
                    Text("Finish a walk to earn your first leaf", fontSize = 13.sp, color = Color(0xFFBBC3CE), textAlign = TextAlign.Center)
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    walks.chunked(6).forEachIndexed { chunkIdx, chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            chunk.forEachIndexed { localIdx, _ ->
                                val i     = chunkIdx * 6 + localIdx
                                Leaf(
                                    color    = leafColors[i % leafColors.size],
                                    size     = leafSizeDps[i % leafSizeDps.size].dp,
                                    rotation = leafRotations[i % leafRotations.size]
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Leaf(color: Color, size: Dp, rotation: Float) {
    Icon(
        Icons.Default.Eco, null,
        tint = color,
        modifier = Modifier.size(size).graphicsLayer { rotationZ = rotation }
    )
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
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Text(label, fontSize = 12.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
        }
    }
}
