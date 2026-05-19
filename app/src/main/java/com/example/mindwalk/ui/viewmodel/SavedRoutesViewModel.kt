package com.example.mindwalk.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindwalk.data.RouteRepository
import com.example.mindwalk.data.SavedRoute
import kotlinx.coroutines.launch

/**
 * ViewModel managing the list of user-saved routes.
 *
 * Exposes [routes] as Compose observable state so [com.example.mindwalk.ui.screens.SavedRoutesScreen]
 * recomposes automatically when the list changes. All database operations are performed on
 * [viewModelScope] coroutines; the [RouteRepository] handles threading internally.
 *
 * The list is loaded eagerly in [init] so the screen shows saved routes immediately on first
 * composition without a visible loading state.
 *
 * @param app Application context used to initialise [RouteRepository].
 */
class SavedRoutesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = RouteRepository(app.applicationContext)

    /**
     * Current list of saved routes, ordered from most recently saved to oldest.
     * Backed by Compose [mutableStateOf] so the UI recomposes on every update.
     */
    var routes by mutableStateOf<List<SavedRoute>>(emptyList())
        private set

    init {
        reload()
    }

    /**
     * Reloads the saved routes list from the database.
     *
     * Called on screen enter ([com.example.mindwalk.ui.screens.SavedRoutesScreen] uses
     * `LaunchedEffect(Unit)`) to pick up any routes saved from a previous session.
     */
    fun reload() {
        viewModelScope.launch {
            routes = repository.getAll()
        }
    }

    /**
     * Persists a new saved route and refreshes [routes].
     *
     * Called from [com.example.mindwalk.ui.screens.PostWalkReflectionScreen] when the user
     * enables the "Save this route" toggle and taps "Complete walk".
     *
     * @param route The [SavedRoute] to store.
     */
    fun save(route: SavedRoute) {
        viewModelScope.launch {
            repository.save(route)
            routes = repository.getAll()
        }
    }

    /**
     * Deletes a saved route by its unique identifier and refreshes [routes].
     *
     * Called from [com.example.mindwalk.ui.screens.SavedRoutesScreen] when the user
     * confirms deletion via the alert dialog.
     *
     * @param id The [SavedRoute.id] of the entry to remove.
     */
    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            routes = repository.getAll()
        }
    }
}
