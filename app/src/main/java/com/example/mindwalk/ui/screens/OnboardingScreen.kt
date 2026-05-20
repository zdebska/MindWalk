package com.example.mindwalk.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mindwalk.ui.theme.*
import com.example.mindwalk.ui.viewmodel.OnboardingPhase
import com.example.mindwalk.ui.viewmodel.OnboardingViewModel

/**
 * City selection and backend graph preparation screen.
 *
 * Used for both first-launch onboarding and the "Change city" flow triggered from
 * [com.example.mindwalk.ui.screens.HomeScreen]. Driven by [OnboardingViewModel] which manages
 * a three-phase state machine:
 * - **CITY_SELECT** — GPS detection button, Nominatim autocomplete text field, and confirm button.
 * - **LOADING**     — animated leaf icon, live status message from the backend polling loop,
 *                     and a [LinearProgressIndicator].
 * - **ERROR**       — error message with a retry button that resets to CITY_SELECT.
 *
 * Phase transitions are animated via [AnimatedContent]. Location permission is requested via
 * [rememberLauncherForActivityResult] when the user taps "Use my location".
 *
 * @param vm         [OnboardingViewModel] owning all onboarding state.
 * @param onComplete Navigates to [com.example.mindwalk.ui.screens.HomeScreen] on success.
 * @param onBack     When non-null, a back button is shown and the screen title changes to
 *                   "Change city". Used in the city-change flow; null on first-launch onboarding.
 */
@Composable
fun OnboardingScreen(
    vm: OnboardingViewModel,
    onComplete: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.detectCityFromGps(context)
        else vm.retry()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Green50, Blue50)))
    ) {
        AnimatedContent(targetState = vm.phase, label = "phase") { phase ->
            when (phase) {
                OnboardingPhase.CITY_SELECT -> CitySelectContent(
                    vm = vm,
                    isChangingCity = onBack != null,
                    onBack = onBack,
                    onGpsTap = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) vm.detectCityFromGps(context)
                        else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    onConfirm = { vm.startPrepare(context, onComplete) }
                )
                OnboardingPhase.LOADING -> LoadingContent(vm.statusMessage)
                OnboardingPhase.ERROR   -> ErrorContent(
                    message = vm.errorMessage ?: "Something went wrong.",
                    onRetry = { vm.retry() }
                )
            }
        }
    }
}

@Composable
private fun CitySelectContent(
    vm: OnboardingViewModel,
    isChangingCity: Boolean = false,
    onBack: (() -> Unit)? = null,
    onGpsTap: () -> Unit,
    onConfirm: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start  = 28.dp,
                    end    = 28.dp,
                    top    = if (isChangingCity) 64.dp else 0.dp,
                    bottom = 96.dp   // leave room for the pinned confirm button
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Icon(
            Icons.Default.Eco,
            contentDescription = null,
            tint = Green600,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            if (isChangingCity) "Change city" else "Welcome to MindWalk",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center
        )
        Text(
            if (isChangingCity) "Choose a new city for your walks."
            else "To get started, tell us which city you'd like to walk in.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.height(36.dp))

        // GPS option
        Surface(
            color = Color.White.copy(alpha = 0.8f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onGpsTap,
                    enabled = !vm.isDetectingLocation,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    if (vm.isDetectingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Detecting location…", color = Color.White)
                    } else {
                        Icon(Icons.Default.MyLocation, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Use my location", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }

                if (vm.detectedCity != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Green600, modifier = Modifier.size(16.dp))
                        Text("Detected: ${vm.detectedCity}", fontSize = 13.sp, color = Green600)
                    }
                }

                if (vm.locationError) {
                    Text(
                        "Couldn't detect location. Please enter your city below.",
                        fontSize = 12.sp,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFD1D5DB))
            Text("or", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            HorizontalDivider(Modifier.weight(1f), color = Color(0xFFD1D5DB))
        }

        Spacer(Modifier.height(16.dp))

        // City autocomplete
        Column {
            OutlinedTextField(
                value = vm.cityInput,
                onValueChange = { vm.onCityInputChanged(it) },
                label = { Text("City name") },
                placeholder = { Text("e.g. Prague, Czechia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 14.dp, topEnd = 14.dp,
                    bottomStart = if (vm.suggestions.isEmpty()) 14.dp else 0.dp,
                    bottomEnd   = if (vm.suggestions.isEmpty()) 14.dp else 0.dp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    vm.dismissSuggestions()
                    if (vm.cityInput.isNotBlank()) onConfirm()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green600,
                    focusedLabelColor  = Green600
                ),
                trailingIcon = {
                    if (vm.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Green600
                        )
                    }
                }
            )

            AnimatedVisibility(
                visible = vm.suggestions.isNotEmpty(),
                enter = expandVertically(),
                exit  = shrinkVertically()
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        vm.suggestions.forEachIndexed { index, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        keyboard?.hide()
                                        vm.selectSuggestion(suggestion)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Green600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(suggestion, fontSize = 14.sp, color = Color(0xFF1F2937))
                            }
                            if (index < vm.suggestions.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                            }
                        }
                    }
                }
            }
        }

        } // end Column

        // Confirm button pinned to the bottom of the screen
        Button(
            onClick = { keyboard?.hide(); onConfirm() },
            enabled = vm.cityInput.isNotBlank(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .navigationBarsPadding()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Text(
                if (isChangingCity) "Change city" else "Set up my city",
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }

        // Back button floated at top-start, rendered last so it draws on top
        if (isChangingCity && onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF374151)
                )
            }
        }
    } // end Box
}

@Composable
private fun LoadingContent(statusMessage: String) {
    val inf = rememberInfiniteTransition(label = "leaf")
    val bounce by inf.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(bounce)
                .background(Green100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Eco, null, tint = Green600, modifier = Modifier.size(72.dp))
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "Preparing your city…",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            statusMessage,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(28.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp),
            color = Green600,
            trackColor = Green100
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "This may take a minute on first run",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Setup failed",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Spacer(Modifier.height(10.dp))

        Text(
            message,
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green600)
        ) {
            Text("Try again", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}
