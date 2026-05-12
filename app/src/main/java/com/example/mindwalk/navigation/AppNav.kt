package com.example.mindwalk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mindwalk.ui.screens.*
import com.example.mindwalk.ui.viewmodel.PlanViewModel
import com.example.mindwalk.ui.viewmodel.SavedRoutesViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val vm: PlanViewModel          = viewModel()
    val savedVm: SavedRoutesViewModel = viewModel()

    // Pop back to HOME and navigate to a top-level tab (for bottom nav switches).
    fun switchTab(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { inclusive = false; saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onGenerateWalk = { navController.navigate(Routes.PLAN) },
                onQuickOption  = { mode -> navController.navigate("${Routes.PLAN}?mode=$mode") },
                onSaved        = { switchTab(Routes.SAVED) },
                onJourney      = { switchTab(Routes.JOURNEY) }
            )
        }

        // ── Plan — optional ?mode= query param ────────────────────────────────
        composable(
            route = "${Routes.PLAN}?mode={mode}",
            arguments = listOf(navArgument("mode") { defaultValue = "" })
        ) { back ->
            // Track whether the user triggered a generation in this session.
            var pendingNav by remember { mutableStateOf(false) }

            // Navigate to PREVIEW only when loading finishes without an error.
            LaunchedEffect(vm.isLoading, vm.routeError) {
                if (pendingNav && !vm.isLoading && !vm.routeError) {
                    pendingNav = false
                    navController.navigate(Routes.PREVIEW)
                }
            }

            PlanYourWalkScreen(
                vm                  = vm,
                preSelectedMode     = back.arguments?.getString("mode") ?: "",
                onBack              = { if (!vm.isLoading) navController.popBackStack() },
                onDismissError      = { pendingNav = false; vm.clearError() },
                onSelectLocation    = { navController.navigate(Routes.LOCATION_PICKER) },
                onSelectEndLocation = { navController.navigate(Routes.END_LOCATION_PICKER) },
                onGenerate          = { dur, mode, shape ->
                    pendingNav = true
                    vm.generatePythonRoute(dur, mode, shape)
                }
            )
        }

        // ── Location picker ───────────────────────────────────────────────────
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
                planVm  = vm,
                savedVm = savedVm,
                onComplete = {
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

        // ── Journey / progress ────────────────────────────────────────────────
        composable(Routes.JOURNEY) {
            JourneyScreen(
                onBack  = { navController.popBackStack() },
                onHome  = { switchTab(Routes.HOME) },
                onSaved = { switchTab(Routes.SAVED) }
            )
        }
    }
}
