package com.example.mindwalk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.data.WalkRecord
import com.example.mindwalk.ui.components.BottomNavBar
import com.example.mindwalk.ui.components.BottomNavTab
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.WalkHistoryViewModel
import java.util.Calendar

/** Twelve distinct leaf colours cycling across the leaf board. Each colour represents a different walk. */
private val leafColors = listOf(
    Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF66BB6A),
    Color(0xFF00897B), Color(0xFF004D40), Color(0xFF558B2F),
    Color(0xFF9CCC65), Color(0xFFFFB300), Color(0xFFF57C00),
    Color(0xFFE64A19), Color(0xFF827717), Color(0xFFF9A825),
)

private val leafRotations = listOf(-15f, 8f, -5f, 20f, -12f, 5f, -18f, 10f, -3f, 15f, -8f, 22f)
private val leafSizeDps   = listOf(28, 24, 32, 26, 30, 22, 28, 34, 24, 28, 26, 30)

/**
 * Journey screen displaying the user's complete walking history and visual leaf collection.
 *
 * Statistics (total walks, walks this month, most popular mode) are computed from the
 * [WalkHistoryViewModel.walks] list each time it changes. The leaf board renders one
 * [Leaf] icon per completed walk, cycling through 12 colours and rotation offsets to give
 * each leaf a distinct appearance.
 *
 * Walk data is reloaded from the Room database on every screen entry via `LaunchedEffect(Unit)`.
 *
 * @param walkHistoryVm Provides the list of all completed [com.example.mindwalk.data.WalkRecord]s.
 * @param onBack        Navigates back (pops the back stack).
 * @param onHome        Switches the bottom nav tab to [HomeScreen].
 * @param onSaved       Switches the bottom nav tab to [SavedRoutesScreen].
 */
@Composable
fun JourneyScreen(
    walkHistoryVm: WalkHistoryViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onSaved: () -> Unit
) {
    LaunchedEffect(Unit) { walkHistoryVm.reload() }

    val walks = walkHistoryVm.walks

    val totalWalks = walks.size

    val walksThisMonth = remember(walks) {
        val cal   = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH)
        val year  = cal.get(Calendar.YEAR)
        walks.count {
            val c = Calendar.getInstance().apply { timeInMillis = it.completedAt }
            c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
        }
    }

    val popularMode = remember(walks) {
        walks.groupingBy { it.mode }.eachCount().maxByOrNull { it.value }?.key ?: "—"
    }

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    JourneyStatCard(totalWalks.toString(),     "Total walks", Modifier.weight(1f))
                    JourneyStatCard(walksThisMonth.toString(), "This month",  Modifier.weight(1f))
                    JourneyStatCard(popularMode,               "Top mode",    Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))

                // LeafBoard fills all remaining vertical space
                LeafBoard(walks, modifier = Modifier.weight(1f))
            }
        }
    }
}

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
            // Title row inside the board
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Leaf collection",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    if (walks.isEmpty()) "No leaves yet"
                    else "${walks.size} leaf${if (walks.size != 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            if (walks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color(0xFFD1D5DB),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        "Your board is empty",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Text(
                        "Finish a walk to earn your first leaf",
                        fontSize = 13.sp,
                        color = Color(0xFFBBC3CE),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
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
                                val color = leafColors[i % leafColors.size]
                                val rot   = leafRotations[i % leafRotations.size]
                                val size  = leafSizeDps[i % leafSizeDps.size].dp
                                Leaf(color = color, size = size, rotation = rot)
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
        imageVector = Icons.Default.Eco,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation }
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
