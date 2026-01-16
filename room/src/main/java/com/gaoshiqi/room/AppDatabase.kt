package com.gaoshiqi.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AnimeEntity::class, SavedPointEntity::class, SearchHistoryEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun savedPointDao(): SavedPointDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 3 to 4:
         * - Add collectionStatus column (default = 3, meaning "在看")
         * - Add watchedEpisodes column (default = 0)
         * - Add totalEpisodes column (default = 0)
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookmarked_anime ADD COLUMN collectionStatus INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE bookmarked_anime ADD COLUMN watchedEpisodes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE bookmarked_anime ADD COLUMN totalEpisodes INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anime_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
