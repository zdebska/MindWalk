package com.example.mindwalk.navigation

/**
 * Navigation route constants used throughout [AppNav].
 *
 * Each constant corresponds to a composable destination registered in the NavHost.
 * Query parameters are appended at the call site (e.g. `"${Routes.PLAN}?mode=Green&duration=20"`).
 */
object Routes {
    /** First-launch onboarding screen for city selection and graph preparation. */
    const val ONBOARDING          = "onboarding"

    /** Main home screen with greeting, preset walk cards, and bottom navigation. */
    const val HOME                = "home"

    /** Walk planning screen where the user configures duration, mode, and start location. */
    const val PLAN                = "plan"

    /** Route preview screen showing the generated route on a full-screen map. */
    const val PREVIEW             = "preview"

    /** Map-based start location picker. */
    const val LOCATION_PICKER     = "location_picker"

    /** Map-based end location picker for point-to-point (Line) routes. */
    const val END_LOCATION_PICKER = "end_location_picker"

    /** Active walking screen with turn-by-turn navigation and mindful prompts. */
    const val WALKING             = "walking"

    /** Post-walk reflection screen for mood selection and optional route saving. */
    const val REFLECTION          = "reflection"

    /** Journey screen showing walk statistics and the leaf collection. */
    const val JOURNEY             = "journey"

    /** Saved routes screen listing all user-saved routes. */
    const val SAVED               = "saved"

    /** City-change screen, reusing the onboarding UI for an already-configured user. */
    const val CHANGE_CITY         = "change_city"
}
