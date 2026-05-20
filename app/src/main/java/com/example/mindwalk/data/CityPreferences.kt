package com.example.mindwalk.data

import android.content.Context

/**
 * SharedPreferences wrapper for persisting the user's configured city.
 *
 * The city name is saved on first-launch onboarding completion and read at every subsequent
 * app start to decide the navigation start destination: if a city is already stored the app
 * opens at [com.example.mindwalk.ui.screens.HomeScreen]; otherwise it shows
 * [com.example.mindwalk.ui.screens.OnboardingScreen].
 *
 * The city is also supplied as the `place` parameter in every `POST /route` request, so it
 * must be in the Nominatim-compatible format `"City, Country"` (e.g. `"Brno, Czechia"`).
 *
 * SharedPreferences is used instead of Room because this single scalar value is needed
 * synchronously at startup, before the database can be opened.
 */
object CityPreferences {

    private const val PREFS_NAME = "city_prefs"
    private const val KEY_CITY   = "configured_city"

    /**
     * Returns the stored city name, or `null` if no city has been configured yet (first launch).
     *
     * @param context Any [Context]; used to access SharedPreferences.
     * @return The city string (e.g. `"Brno, Czechia"`), or `null` on first launch.
     */
    fun getCity(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CITY, null)

    /**
     * Persists the configured city name.
     *
     * Called by [com.example.mindwalk.ui.viewmodel.OnboardingViewModel.startPrepare]
     * after the backend confirms the city graph is ready.
     *
     * @param context Any [Context]; used to access SharedPreferences.
     * @param city    The city string to store, in `"City, Country"` format.
     */
    fun setCity(context: Context, city: String) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_CITY, city).apply()
}
