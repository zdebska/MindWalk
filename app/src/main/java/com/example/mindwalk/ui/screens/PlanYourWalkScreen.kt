package com.example.mindwalk.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mindwalk.ui.components.ChoiceChip
import com.example.mindwalk.ui.components.PrimaryButton
import com.example.mindwalk.ui.components.SectionCard
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.PlanViewModel

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun PlanYourWalkScreen(
    vm: PlanViewModel,
    preSelectedMode: String = "",
    onBack: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onSelectLocation: () -> Unit,
    onSelectEndLocation: () -> Unit = {},
    onGenerate: (durationMin: Int, mode: String, shape: String) -> Unit
) {
    val context = LocalContext.current
    var duration by rememberSaveable { mutableStateOf(30) }
    var mindfulnessMode by rememberSaveable { mutableStateOf(preSelectedMode.ifEmpty { "Calm" }) }
    var routeShape by rememberSaveable { mutableStateOf("Loop") }
    var locationOfflineError by remember { mutableStateOf(false) }
    var offlineAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Green50, Blue50)))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Surface(
                    color = Color.White.copy(alpha = 0.85f),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrimaryButton(
                            text = if (vm.isLoading) "Generating…" else "Generate Route"
                        ) {
                            if (!vm.isLoading) onGenerate(duration, mindfulnessMode, routeShape)
                        }
                        OutlinedButton(
                            onClick = { if (!vm.isLoading) onGenerate(duration, mindfulnessMode, routeShape) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp
                            )
                        ) {
                            Text(
                                "Auto-generate Nearby Loop",
                                color = Color(0xFF374151),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (!vm.isLoading) onBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF374151)
                            )
                        }
                        Column(Modifier.padding(start = 4.dp)) {
                            Text(
                                "Plan Your Walk",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                "Create a mindful walking route",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                // Route Shape
                item {
                    SectionCard(title = "Route Shape") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChoiceChip("Loop", routeShape == "Loop", { routeShape = "Loop" }, Modifier.weight(1f))
                            ChoiceChip("Line", routeShape == "Line", { routeShape = "Line" }, Modifier.weight(1f))
                        }
                    }
                }

                // Start / End
                item {
                    SectionCard(title = "Start / End") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isNetworkAvailable(context)) onSelectLocation()
                                        else { offlineAction = { onSelectLocation() }; locationOfflineError = true }
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = Green600, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Start", fontSize = 12.sp, color = Color(0xFF6B7280))
                                    Text(vm.startLocationName, fontSize = 15.sp, color = Color(0xFF1F2937))
                                }
                            }
                            if (routeShape == "Line") {
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isNetworkAvailable(context)) onSelectEndLocation()
                                            else { offlineAction = { onSelectEndLocation() }; locationOfflineError = true }
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("End", fontSize = 12.sp, color = Color(0xFF6B7280))
                                        Text(vm.endLocationName, fontSize = 15.sp, color = Color(0xFF1F2937))
                                    }
                                }
                            }
                        }
                    }
                }

                // Walk Goals
                item {
                    SectionCard(title = "Walk Goals") {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Duration", color = Color(0xFF6B7280), fontSize = 14.sp)
                            Text(
                                "$duration min",
                                color = Green600,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        Slider(
                            value = duration.toFloat(),
                            onValueChange = { duration = it.toInt() },
                            valueRange = 10f..120f,
                            steps = 21,
                            colors = SliderDefaults.colors(
                                thumbColor = Green600,
                                activeTrackColor = Green600,
                                inactiveTrackColor = Green100
                            )
                        )
                    }
                }

                // Mindfulness Mode
                item {
                    SectionCard(title = "Mindfulness Mode") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChoiceChip("Calm",    mindfulnessMode == "Calm",   { mindfulnessMode = "Calm" },    Modifier.weight(1f))
                            ChoiceChip("Scenic",  mindfulnessMode == "Scenic", { mindfulnessMode = "Scenic" },  Modifier.weight(1f))
                            ChoiceChip("Explore", mindfulnessMode == "Explore",{ mindfulnessMode = "Explore" }, Modifier.weight(1f))
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    when {
        vm.isLoading       -> LoadingDialog()
        vm.routeError      -> ErrorDialog(
            onRetry   = { vm.retryRoute() },
            onDismiss = onDismissError
        )
        locationOfflineError -> LocationOfflineDialog(
            onRetry   = {
                if (isNetworkAvailable(context)) {
                    locationOfflineError = false
                    offlineAction?.invoke()
                    offlineAction = null
                }
            },
            onDismiss = { locationOfflineError = false; offlineAction = null }
        )
    }
}

@Composable
private fun LoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    color = Green600,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(52.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Finding your route…",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "This may take a moment",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(52.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Couldn't reach the server",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Check your connection and try again",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Text("Try again", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss", color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationOfflineDialog(onRetry: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(52.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "No internet connection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Connect to the internet to pick a start location on the map",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green600)
                    ) {
                        Text("Try again", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Dismiss", color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}

