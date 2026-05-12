package com.example.mindwalk.data

import androidx.room.*

@Dao
interface SavedRouteDao {
    @Query("SELECT * FROM saved_routes ORDER BY savedAt DESC")
    suspend fun getAll(): List<SavedRoute>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: SavedRoute)

    @Query("DELETE FROM saved_routes WHERE id = :id")
    suspend fun delete(id: String)
}
