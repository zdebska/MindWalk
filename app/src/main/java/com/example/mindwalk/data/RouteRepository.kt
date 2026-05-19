package com.example.mindwalk.data

import android.content.Context

/**
 * Single access point for all Room database operations in the MindWalk app.
 *
 * [RouteRepository] wraps [MindWalkDatabase] and exposes suspend functions for both
 * the `saved_routes` and `walk_records` tables. ViewModels call this class from
 * `viewModelScope` coroutines; the class itself does not manage threading.
 *
 * @param context Used to obtain the [MindWalkDatabase] singleton via [MindWalkDatabase.get].
 *                The application context is extracted inside [MindWalkDatabase] to prevent leaks.
 */
class RouteRepository(context: Context) {

    private val db = MindWalkDatabase.get(context)

    // ── Saved routes ──────────────────────────────────────────────────────────

    /**
     * Returns all saved routes, ordered from most recently saved to oldest.
     *
     * @return List of [SavedRoute] sorted by [SavedRoute.savedAt] descending.
     */
    suspend fun getAll(): List<SavedRoute> = db.savedRouteDao().getAll()

    /**
     * Persists a new saved route, replacing any existing entry with the same id.
     *
     * @param route The [SavedRoute] to insert or update.
     */
    suspend fun save(route: SavedRoute) = db.savedRouteDao().insert(route)

    /**
     * Deletes a saved route by its unique identifier.
     *
     * @param id The [SavedRoute.id] of the entry to remove.
     */
    suspend fun delete(id: String) = db.savedRouteDao().delete(id)

    // ── Walk history ──────────────────────────────────────────────────────────

    /**
     * Returns all completed walk records, ordered from most recent to oldest.
     *
     * @return List of [WalkRecord] sorted by [WalkRecord.completedAt] descending.
     */
    suspend fun getAllWalks(): List<WalkRecord> = db.walkRecordDao().getAll()

    /**
     * Records a completed walk session, replacing any existing entry with the same id.
     *
     * @param walk The [WalkRecord] to persist.
     */
    suspend fun recordWalk(walk: WalkRecord) = db.walkRecordDao().insert(walk)
}
