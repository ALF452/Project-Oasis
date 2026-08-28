package com.oasis.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [GameEntity::class, LogEntryEntity::class, BacklogEntity::class], version = 4, exportSchema = false)
abstract class OasisDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun backlogDao(): BacklogDao

    companion object {
        @Volatile
        private var instance: OasisDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN steamAppId INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE log_entries ADD COLUMN rating REAL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS backlog_games (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        coverUrl TEXT,
                        sourceUrl TEXT,
                        summary TEXT,
                        addedAt INTEGER NOT NULL
                    )
                    """
                )
            }
        }

        fun getInstance(context: Context): OasisDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OasisDatabase::class.java,
                    "oasis.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build().also { instance = it }
            }
    }
}
