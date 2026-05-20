package com.example.mindwalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mindwalk.navigation.AppNav
import com.example.mindwalk.ui.theme.MindWalkTheme
import org.osmdroid.config.Configuration

/**
 * Single Activity that hosts the entire MindWalk application.
 *
 * MindWalk follows the single-Activity architecture pattern: all screens are Jetpack Compose
 * composable destinations managed by [AppNav] inside a single [NavHost]. No Fragment or
 * secondary Activity is used.
 *
 * **Responsibilities:**
 * - Configures the OSMDroid tile-cache user agent to the application package name before
 *   the first map view is created, which is required by the OSM tile server usage policy.
 * - Wraps the entire composition in [MindWalkTheme] so all screens share a consistent
 *   Material 3 colour scheme and typography.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid requires a non-empty user agent string identifying the app
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            MindWalkTheme {
                AppNav()
            }
        }
    }
}
