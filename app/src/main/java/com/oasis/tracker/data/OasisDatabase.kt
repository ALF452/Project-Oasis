package com.oasis.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GameEntity::class, LogEntryEntity::class], version = 2, exportSchema = false)
abstract class OasisDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun logEntryDao(): LogEntryDao

    companion object {
        @Volatile
        private var instance: OasisDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN steamAppId INTEGER")
            }
        }

        fun getInstance(context: Context): OasisDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OasisDatabase::class.java,
                    "oasis.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
