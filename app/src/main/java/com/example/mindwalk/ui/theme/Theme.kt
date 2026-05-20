package com.example.mindwalk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Material 3 dark colour scheme using the default scaffold purple palette.
 *
 * Not actively used by MindWalk screens (all screens use explicit colour tokens from
 * [com.example.mindwalk.ui.theme.Color]), but required by [MindWalkTheme] when
 * `dynamicColor` is unavailable and the device is in dark mode.
 */
private val DarkColorScheme = darkColorScheme(
    primary   = Purple80,
    secondary = PurpleGrey80,
    tertiary  = Pink80
)

/**
 * Material 3 light colour scheme using the default scaffold purple palette.
 *
 * Serves as the static fallback when dynamic colour (Android 12+) is disabled or
 * unavailable and the device is in light mode.
 */
private val LightColorScheme = lightColorScheme(
    primary   = Purple40,
    secondary = PurpleGrey40,
    tertiary  = Pink40
)

/**
 * Root Material 3 theme composable wrapping the entire MindWalk application.
 *
 * **Colour resolution priority:**
 * 1. Dynamic colour (Android 12+ / API 31+) — wallpaper-derived palette, respects dark mode.
 * 2. [DarkColorScheme] — if dark mode is on but dynamic colour is unavailable.
 * 3. [LightColorScheme] — the static light-mode fallback.
 *
 * In addition to colour, [MindWalkTheme] applies the app [Typography] and synchronises the
 * system status bar colour with the resolved primary colour via a [SideEffect].
 *
 * Note: individual screens override colours directly using the tokens defined in
 * [com.example.mindwalk.ui.theme.Color] rather than reading from [MaterialTheme.colorScheme],
 * so the exact scheme resolved here has minimal visual impact on screen content.
 *
 * @param darkTheme    Whether to apply a dark colour scheme. Defaults to the system setting.
 * @param dynamicColor Whether to use the Android 12+ dynamic colour API. Defaults to true.
 * @param content      The composable subtree to which this theme is applied.
 */
@Composable
fun MindWalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
