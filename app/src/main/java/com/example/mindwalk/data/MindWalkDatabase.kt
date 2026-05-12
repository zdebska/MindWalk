package com.example.mindwalk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SavedRoute::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MindWalkDatabase : RoomDatabase() {
    abstract fun savedRouteDao(): SavedRouteDao

    companion object {
        @Volatile private var INSTANCE: MindWalkDatabase? = null

        fun get(context: Context): MindWalkDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MindWalkDatabase::class.java,
                    "mindwalk.db"
                ).build().also { INSTANCE = it }
            }
    }
}
