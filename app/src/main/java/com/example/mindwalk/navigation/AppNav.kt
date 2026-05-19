package com.example.mindwalk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mindwalk.data.CityPreferences
import com.example.mindwalk.ui.screens.*
import com.example.mindwalk.ui.viewmodel.OnboardingViewModel
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import com.example.mindwalk.ui.viewmodel.SavedRoutesViewModel
import com.example.mindwalk.ui.viewmodel.WalkHistoryViewModel

/**
 * Root navigation composable for the MindWalk application.
 *
 * Owns all ViewModel instances so they survive screen transitions and are shared correctly
 * across related destinations (e.g. [PlanViewModel] is shared between the planning, preview,
 * walking, and reflection screens).
 *
 * The start destination is determined once at composition time by checking [CityPreferences]:
 * - If a city is already configured → [Routes.HOME]
 * - Otherwise → [Routes.ONBOARDING]
 *
 * Bottom-navigation tab switches use [switchTab] which preserves back-stack state and
 * avoids duplicate destinations in the back stack.
 */
@Composable
fun AppNav() {
    val context       = LocalContext.current
    val navController = rememberNavController()

    val vm: PlanViewModel                    = viewModel()
    val savedVm: SavedRoutesViewModel        = viewModel()
    val walkHistoryVm: WalkHistoryViewModel  = viewModel()
    val onboardingVm: OnboardingViewModel    = viewModel()

    /** Determined once — either ONBOARDING (first launch) or HOME (returning user). */
    val startDest = remember {
        if (CityPreferences.getCity(context) != null) Routes.HOME else Routes.ONBOARDING
    }

    /**
     * Navigates to a top-level tab destination while preserving back-stack state.
     *
     * Pops up to (but not including) [Routes.HOME] so the back stack stays shallow,
     * and restores previously saved state when re-entering a tab.
     */
    fun switchTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { inclusive = false; saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    NavHost(navController = navController, startDestination = startDest) {

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                vm = onboardingVm,
                onComplete = {
                    // Remove ONBOARDING from the back stack so Back doesn't return to it
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onGenerateWalk = { navController.navigate(Routes.PLAN) },
                onQuickOption  = { mode, duration ->
                    // Preset cards pass both mode and duration as query parameters
                    navController.navigate("${Routes.PLAN}?mode=$mode&duration=$duration")
                },
                onSaved      = { switchTab(Routes.SAVED) },
                onJourney    = { switchTab(Routes.JOURNEY) },
                onChangeCity = { navController.navigate(Routes.CHANGE_CITY) }
            )
        }

        // ── Plan — optional ?mode= and ?duration= query params ────────────────
        // Defaults of "" and 0 apply when the user navigates from the plain "Generate a walk" button.
        composable(
            route = "${Routes.PLAN}?mode={mode}&duration={duration}",
            arguments = listOf(
                navArgument("mode")     { defaultValue = "" },
                navArgument("duration") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { back ->
            // Track whether a generation was triggered in this back-stack entry
            var pendingNav by remember { mutableStateOf(false) }

            // Navigate to PREVIEW only when the loading finishes without an error
            LaunchedEffect(vm.isLoading, vm.routeError) {
                if (pendingNav && !vm.isLoading && !vm.routeError) {
                    pendingNav = false
                    navController.navigate(Routes.PREVIEW)
                }
            }

            PlanYourWalkScreen(
                vm                  = vm,
                preSelectedMode     = back.arguments?.getString("mode") ?: "",
                preSelectedDuration = back.arguments?.getInt("duration") ?: 0,
                onBack              = { if (!vm.isLoading) navController.popBackStack() },
                onDismissError      = { pendingNav = false; vm.clearError() },
                onSelectLocation    = { navController.navigate(Routes.LOCATION_PICKER) },
                onGenerate          = { dur, mode ->
                    pendingNav = true
                    vm.generatePythonRoute(dur, mode)
                }
            )
        }

        // ── Start location picker ─────────────────────────────────────────────
        composable(Routes.LOCATION_PICKER) {
            LocationPickerScreen(vm = vm, isStart = true, onBack = { navController.popBackStack() })
        }

        // ── End location picker ───────────────────────────────────────────────
        composable(Routes.END_LOCATION_PICKER) {
            LocationPickerScreen(vm = vm, isStart = false, onBack = { navController.popBackStack() })
        }

        // ── Route preview ─────────────────────────────────────────────────────
        composable(Routes.PREVIEW) {
            RoutePreviewScreen(
                vm          = vm,
                onBack      = { navController.popBackStack() },
                onStartWalk = { navController.navigate(Routes.WALKING) }
            )
        }

        // ── Active walking ────────────────────────────────────────────────────
        composable(Routes.WALKING) {
            WalkingScreen(vm = vm, onEndWalk = { navController.navigate(Routes.REFLECTION) })
        }

        // ── Post-walk reflection ──────────────────────────────────────────────
        composable(Routes.REFLECTION) {
            PostWalkReflectionScreen(
                planVm        = vm,
                savedVm       = savedVm,
                walkHistoryVm = walkHistoryVm,
                onComplete    = {
                    // Clear the back stack to HOME so Back doesn't re-enter the walk flow
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // ── Saved routes ──────────────────────────────────────────────────────
        composable(Routes.SAVED) {
            SavedRoutesScreen(
                savedVm         = savedVm,
                onBack          = { navController.popBackStack() },
                onHome          = { switchTab(Routes.HOME) },
                onJourney       = { switchTab(Routes.JOURNEY) },
                onRouteSelected = { route ->
                    vm.loadSavedRoute(route)
                    navController.navigate(Routes.PREVIEW)
                }
            )
        }

        // ── Change city ───────────────────────────────────────────────────────
        // Reuses OnboardingScreen with onBack set, which changes the title and adds a back button.
        // OnboardingViewModel is reset to CITY_SELECT so the screen starts fresh each time.
        composable(Routes.CHANGE_CITY) {
            LaunchedEffect(Unit) {
                onboardingVm.retry()
                // Pre-fill the text field with the currently saved city
                val saved = CityPreferences.getCity(context) ?: ""
                onboardingVm.cityInput = saved
            }
            OnboardingScreen(
                vm = onboardingVm,
                onComplete = {
                    // Pop back to HOME after the new city is prepared
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Journey / progress ────────────────────────────────────────────────
        composable(Routes.JOURNEY) {
            JourneyScreen(
                walkHistoryVm = walkHistoryVm,
                onBack        = { navController.popBackStack() },
                onHome        = { switchTab(Routes.HOME) },
                onSaved       = { switchTab(Routes.SAVED) }
            )
        }
    }
}
