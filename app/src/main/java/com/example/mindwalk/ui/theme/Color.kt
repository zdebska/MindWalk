package com.example.mindwalk.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Material 3 scaffold purple palette — light variant.
 *
 * These three tokens are used by [MindWalkTheme]'s static [LightColorScheme] and
 * [DarkColorScheme] as fallback colours when Android 12+ dynamic colour is unavailable.
 * They are **not** referenced directly by any MindWalk screen composable.
 */
val Purple80     = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80       = Color(0xFFEFB8C8)

/** Material 3 scaffold purple palette — dark variant. See [Purple80] for usage notes. */
val Purple40     = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40       = Color(0xFF7D5260)

/**
 * Nature-inspired green palette used throughout MindWalk screens.
 *
 * Shades follow the Tailwind CSS naming convention (50 = lightest, 700 = darkest).
 * Common usages:
 * - [Green50] / [Green100] — chip and card backgrounds
 * - [Green400] / [Green500] — progress bars and accent icons
 * - [Green600] — primary action borders, city-row text
 * - [Green700] — active chip labels, heading text
 */
val Green50  = Color(0xFFF0FDF4)
val Green100 = Color(0xFFDCFCE7)
val Green400 = Color(0xFF4ADE80)
val Green500 = Color(0xFF22C55E)
val Green600 = Color(0xFF16A34A)
val Green700 = Color(0xFF15803D)

/**
 * Blue accent palette used for informational badges and mode chips.
 *
 * - [Blue50] / [Blue100] — chip backgrounds (e.g. "Sightseeing" mode)
 * - [Blue600] / [Blue700] — chip borders and label text
 */
val Blue50  = Color(0xFFEFF6FF)
val Blue100 = Color(0xFFDBEAFE)
val Blue600 = Color(0xFF2563EB)
val Blue700 = Color(0xFF1D4ED8)

/**
 * Single-step accent colours used for specific UI elements.
 *
 * - [Yellow500] — "Quiet" mode chip tint
 * - [Red400] / [Red500] — error states and destructive action indicators
 * - [Pink500] — "Creative" or "Wellness" mode chip tint
 * - [Purple500] — "Mindfulness" mode chip tint
 */
val Yellow500 = Color(0xFFEAB308)
val Red400    = Color(0xFFF87171)
val Red500    = Color(0xFFEF4444)
val Pink500   = Color(0xFFEC4899)
val Purple500 = Color(0xFF8B5CF6)