package com.example.uas

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Project::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration logic
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS projects (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "tujuan TEXT NOT NULL, " +
                            "startDate TEXT NOT NULL, " +
                            "endDate TEXT NOT NULL, " +
                            "supervisor TEXT NOT NULL, " +
                            "anggota TEXT NOT NULL, " +
                            "status TEXT NOT NULL, " +
                            "notes TEXT NOT NULL)"
                )
            }
        }
    }
}
