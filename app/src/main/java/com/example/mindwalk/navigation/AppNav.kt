package com.example.mindwalk.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindwalk.ui.screens.PlanYourWalkScreen
import com.example.mindwalk.ui.screens.RoutePreviewScreen
import com.example.mindwalk.ui.screens.LocationPickerScreen
import com.example.mindwalk.ui.viewmodel.PlanViewModel

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val vm: PlanViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.PLAN
    ) {
        composable(Routes.PLAN) {
            PlanYourWalkScreen(
                vm = vm,
                onSelectLocation = {
                    navController.navigate(Routes.LOCATION_PICKER)
                },
                onGenerate = { duration, mode, shape ->
                    vm.generatePythonRoute(duration, mode, shape)
                    navController.navigate(Routes.PREVIEW)
                }
            )
        }

        composable(Routes.PREVIEW) {
            RoutePreviewScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.LOCATION_PICKER) {
            LocationPickerScreen(
                vm = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
