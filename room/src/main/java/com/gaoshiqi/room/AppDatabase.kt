package com.gaoshiqi.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnimeEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anime_database"
                )
                    // 注意：不使用 fallbackToDestructiveMigration()，避免用户数据丢失
                    // 如果需要迁移，请添加 Migration 对象：.addMigrations(MIGRATION_1_2, ...)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
