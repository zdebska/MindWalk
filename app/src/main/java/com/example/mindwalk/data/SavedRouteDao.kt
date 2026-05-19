package com.example.mindwalk.data

import androidx.room.*

/**
 * Room Data Access Object for the `saved_routes` table.
 *
 * Provides suspend functions for querying, inserting, and deleting [SavedRoute] entries.
 * All operations are safe to call from a coroutine on any dispatcher.
 */
@Dao
interface SavedRouteDao {

    /**
     * Returns all saved routes ordered from most recently saved to oldest.
     *
     * @return List of [SavedRoute] sorted by [SavedRoute.savedAt] descending.
     */
    @Query("SELECT * FROM saved_routes ORDER BY savedAt DESC")
    suspend fun getAll(): List<SavedRoute>

    /**
     * Inserts or replaces a saved route. Using [OnConflictStrategy.REPLACE] means
     * re-saving a route with the same [SavedRoute.id] overwrites the previous entry.
     *
     * @param route The [SavedRoute] to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: SavedRoute)

    /**
     * Deletes a saved route by its unique identifier.
     *
     * @param id The [SavedRoute.id] of the entry to remove.
     */
    @Query("DELETE FROM saved_routes WHERE id = :id")
    suspend fun delete(id: String)
}
