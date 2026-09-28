package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String = "book"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long = 0,
    val title: String,
    val dueDateMillis: Long = 0,
    val priority: String = "NORMAL", // URGENT, NORMAL, LATER
    val isCompleted: Boolean = false,
    val isExam: Boolean = false,
    val examDateMillis: Long = 0,
    val xpAwarded: Int = 20
)

@Entity(tableName = "prayers")
data class PrayerRecordEntity(
    @PrimaryKey val dateString: String, // yyyy-MM-dd
    val fajrDone: Boolean = false,
    val dhuhrDone: Boolean = false,
    val asrDone: Boolean = false,
    val maghribDone: Boolean = false,
    val ishaDone: Boolean = false
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String,
    val title: String,
    val type: String, // HOME, GYM, CARDIO
    val durationMinutes: Int,
    val setsReps: String = "",
    val notes: String = "",
    val xpEarned: Int = 30,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val icon: String, // emoji or icon name
    val frequency: String = "DAILY",
    val streakCount: Int = 0,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateString: String
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "طالب مميز",
    val totalXp: Int = 50,
    val level: Int = 1,
    val streakDays: Int = 1,
    val onboardingCompleted: Boolean = false,
    val lastActiveDate: String = "",
    val totalStudyMinutes: Int = 0,
    val totalWorkoutMinutes: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "bennu"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionTaken: String? = null
)
