package com.example.mindwalk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Room Data Access Object for the `walk_records` table.
 *
 * Provides suspend functions for inserting and retrieving [WalkRecord] entries.
 * All operations are safe to call from a coroutine on any dispatcher; Room
 * runs the underlying SQL on its own internal thread pool.
 */
@Dao
interface WalkRecordDao {

    /**
     * Returns all walk records ordered from most recent to oldest.
     *
     * @return List of [WalkRecord] sorted by [WalkRecord.completedAt] descending.
     */
    @Query("SELECT * FROM walk_records ORDER BY completedAt DESC")
    suspend fun getAll(): List<WalkRecord>

    /**
     * Inserts a new walk record. If a record with the same [WalkRecord.id] already exists
     * (e.g. due to a retry), it is silently replaced.
     *
     * @param walk The [WalkRecord] to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walk: WalkRecord)
}
