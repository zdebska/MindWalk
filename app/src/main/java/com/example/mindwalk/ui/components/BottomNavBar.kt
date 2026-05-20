package com.example.mindwalk.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.theme.Green700

/**
 * The three top-level tabs reachable from the bottom navigation bar.
 *
 * Used by [BottomNavBar] to determine which tab is highlighted and by each tab screen
 * to select the appropriate [onTabSelected] branch in [com.example.mindwalk.navigation.AppNav].
 */
enum class BottomNavTab { HOME, SAVED, JOURNEY }

/**
 * Persistent bottom navigation bar shared across the three top-level screens.
 *
 * Renders a Material 3 [NavigationBar] with three items: Home, Saved routes, and Journey.
 * The active tab is indicated by [Green700] icon and label colour; inactive tabs use grey.
 * The indicator background is transparent to match the app's translucent surface style.
 *
 * @param selected      The currently active [BottomNavTab], used to highlight the correct item.
 * @param onTabSelected Called with the tapped [BottomNavTab]; the caller is responsible for
 *                      performing the actual navigation via [com.example.mindwalk.navigation.AppNav.switchTab].
 */
@Composable
fun BottomNavBar(
    selected: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor   = Green700,
        selectedTextColor   = Green700,
        unselectedIconColor = Color.Gray,
        unselectedTextColor = Color.Gray,
        indicatorColor      = Color.Transparent
    )

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.9f),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selected == BottomNavTab.HOME,
            onClick  = { onTabSelected(BottomNavTab.HOME) },
            icon     = { Icon(Icons.Default.Home, "Home", Modifier.size(24.dp)) },
            label    = { Text("Home", fontSize = 12.sp) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = selected == BottomNavTab.SAVED,
            onClick  = { onTabSelected(BottomNavTab.SAVED) },
            icon     = { Icon(Icons.Default.Bookmark, "Saved", Modifier.size(24.dp)) },
            label    = { Text("Saved", fontSize = 12.sp) },
            colors   = itemColors
        )
        NavigationBarItem(
            selected = selected == BottomNavTab.JOURNEY,
            onClick  = { onTabSelected(BottomNavTab.JOURNEY) },
            icon     = { Icon(Icons.Default.Explore, "Journey", Modifier.size(24.dp)) },
            label    = { Text("Journey", fontSize = 12.sp) },
            colors   = itemColors
        )
    }
}
