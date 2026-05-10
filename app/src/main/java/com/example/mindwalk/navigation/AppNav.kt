package com.example.mindwalk.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mindwalk.ui.screens.*
import com.example.mindwalk.ui.viewmodel.PlanViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val vm: PlanViewModel = viewModel()

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
            PlanYourWalkScreen(
                vm               = vm,
                preSelectedMode  = back.arguments?.getString("mode") ?: "",
                onBack           = { navController.popBackStack() },
                onSelectLocation = { navController.navigate(Routes.LOCATION_PICKER) },
                onGenerate       = { dur, mode, shape ->
                    vm.generatePythonRoute(dur, mode, shape)
                    navController.navigate(Routes.PREVIEW)
                }
            )
        }

        // ── Location picker ───────────────────────────────────────────────────
        composable(Routes.LOCATION_PICKER) {
            LocationPickerScreen(vm = vm, onBack = { navController.popBackStack() })
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
                onBack    = { navController.popBackStack() },
                onHome    = { switchTab(Routes.HOME) },
                onJourney = { switchTab(Routes.JOURNEY) }
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
