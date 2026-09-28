package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.PrayerDao
import com.example.data.local.dao.StudyDao
import com.example.data.local.dao.UserProgressDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProgressEntity
import com.example.data.local.entity.WorkoutEntity

@Database(
    entities = [
        SubjectEntity::class,
        TaskEntity::class,
        PrayerRecordEntity::class,
        WorkoutEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        UserProgressEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
    abstract fun prayerDao(): PrayerDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun habitDao(): HabitDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "irtiqa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
