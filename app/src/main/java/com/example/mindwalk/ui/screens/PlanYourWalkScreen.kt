package com.example.mindwalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mindwalk.ui.components.ChoiceChip
import com.example.mindwalk.ui.components.PrimaryButton
import com.example.mindwalk.ui.components.SectionCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import com.example.mindwalk.ui.viewmodel.PlanViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanYourWalkScreen(
    vm: PlanViewModel,
    onGenerate: (durationMin: Int, mode: String, shape: String) -> Unit
) {
    val accent = Color(0xFF6B9E7F)

    var duration by remember { mutableIntStateOf(30) }
    var mindfulnessMode by remember { mutableStateOf("Calm") }
    var routeShape by remember { mutableStateOf("Loop") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plan Your Walk") }) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryButton("Generate Route") {
                    onGenerate(duration, mindfulnessMode, routeShape)
                }

                OutlinedButton(
                    onClick = { onGenerate(duration, mindfulnessMode, routeShape) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Auto-generate Nearby Loop")
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "Create a mindful walking route",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF777777)
                )
            }

            item {
                SectionCard(title = "Today's Progress") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMini("1.2 km", "Distance")
                        StatMini("2,847", "Steps")
                        StatMini("18 min", "Time")
                    }
                }
            }

            item {
                SectionCard(title = "Start / End") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Start", style = MaterialTheme.typography.bodySmall, color = Color(0xFF777777))
                            Text("Current Location")
                        }
                        Column {
                            Text("End", style = MaterialTheme.typography.bodySmall, color = Color(0xFF777777))
                            Text("Same Place")
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Walk Goals") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Duration", color = Color(0xFF666666))
                        Text("$duration min", color = accent)
                    }
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { duration = it.toInt() },
                        valueRange = 10f..120f,
                        steps = 21
                    )
                }
            }

            item {
                SectionCard(title = "Mindfulness Mode") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChoiceChip("Calm", mindfulnessMode == "Calm", { mindfulnessMode = "Calm" }, Modifier.weight(1f))
                        ChoiceChip("Scenic", mindfulnessMode == "Scenic", { mindfulnessMode = "Scenic" }, Modifier.weight(1f))
                        ChoiceChip("Explore", mindfulnessMode == "Explore", { mindfulnessMode = "Explore" }, Modifier.weight(1f))
                    }
                }
            }

            item {
                SectionCard(title = "Route Shape") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChoiceChip("Loop", routeShape == "Loop", { routeShape = "Loop" }, Modifier.weight(1f))
                        ChoiceChip("Line", routeShape == "Line", { routeShape = "Line" }, Modifier.weight(1f))
                        ChoiceChip("Heart", routeShape == "Heart", { routeShape = "Heart" }, Modifier.weight(1f))
                        ChoiceChip("Random", routeShape == "Random", { routeShape = "Random" }, Modifier.weight(1f))
                    }
                }
            }

            // IMPORTANT: add spacer so last card isn’t hidden behind bottom buttons
            item { Spacer(Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun StatMini(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF777777)
        )
    }
}

