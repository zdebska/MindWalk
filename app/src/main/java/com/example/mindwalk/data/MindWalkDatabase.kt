package com.example.mindwalk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migration from schema version 1 to version 2.
 *
 * Version 1 contained only the `saved_routes` table. Version 2 adds the `walk_records`
 * table to track all completed walks independently of whether the user chose to save them.
 * The migration is applied automatically by Room on first open after the app is updated.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `walk_records` (
                `id` TEXT NOT NULL,
                `completedAt` INTEGER NOT NULL,
                `durationMin` INTEGER NOT NULL,
                `distanceKm` REAL NOT NULL,
                `mode` TEXT NOT NULL,
                `shape` TEXT NOT NULL,
                `mood` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )"""
        )
    }
}

/**
 * Central Room database for the MindWalk application.
 *
 * Contains two tables:
 * - `saved_routes` — user-saved routes ([SavedRoute]).
 * - `walk_records` — all completed walk sessions ([WalkRecord]).
 *
 * [Converters] is registered to handle serialisation of the [SavedRoute.points] list.
 *
 * The singleton instance is created lazily on first access and reused for the lifetime of
 * the process. Access is thread-safe via double-checked locking with `@Volatile`.
 */
@Database(entities = [SavedRoute::class, WalkRecord::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MindWalkDatabase : RoomDatabase() {

    /** DAO for [SavedRoute] operations on the `saved_routes` table. */
    abstract fun savedRouteDao(): SavedRouteDao

    /** DAO for [WalkRecord] operations on the `walk_records` table. */
    abstract fun walkRecordDao(): WalkRecordDao

    companion object {

        @Volatile
        private var INSTANCE: MindWalkDatabase? = null

        /**
         * Returns the singleton [MindWalkDatabase] instance, creating it if necessary.
         *
         * Uses the application context to avoid Activity memory leaks. The database file
         * is named `mindwalk.db` and stored in the app's private data directory.
         *
         * @param context Any [Context]; the application context is extracted internally.
         * @return The shared [MindWalkDatabase] instance.
         */
        fun get(context: Context): MindWalkDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MindWalkDatabase::class.java,
                    "mindwalk.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
