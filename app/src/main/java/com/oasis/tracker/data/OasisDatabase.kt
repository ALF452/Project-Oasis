package com.oasis.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class, LogEntryEntity::class], version = 1, exportSchema = false)
abstract class OasisDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile
        private var instance: OasisDatabase? = null

        fun getInstance(context: Context): OasisDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OasisDatabase::class.java,
                    "oasis.db"
                ).build().also { instance = it }
            }
    }
}
