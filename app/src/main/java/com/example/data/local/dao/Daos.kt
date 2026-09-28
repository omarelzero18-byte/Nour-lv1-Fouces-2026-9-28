package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.data.local.entity.PrayerRecordEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProgressEntity
import com.example.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority = 'URGENT' DESC, dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority = 'URGENT' DESC, dueDateMillis ASC")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isExam = 1 AND isCompleted = 0 ORDER BY examDateMillis ASC")
    fun getUpcomingExams(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayers WHERE dateString = :dateString LIMIT 1")
    fun getPrayerRecord(dateString: String): Flow<PrayerRecordEntity?>

    @Query("SELECT * FROM prayers WHERE dateString = :dateString LIMIT 1")
    suspend fun getPrayerRecordSync(dateString: String): PrayerRecordEntity?

    @Query("SELECT * FROM prayers ORDER BY dateString DESC")
    fun getAllPrayerRecords(): Flow<List<PrayerRecordEntity>>

    @Query("SELECT * FROM prayers ORDER BY dateString DESC")
    suspend fun getAllPrayerRecordsSync(): List<PrayerRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: PrayerRecordEntity)

    @Query("SELECT COUNT(*) FROM prayers WHERE fajrDone = 1 AND dhuhrDone = 1 AND asrDone = 1 AND maghribDone = 1 AND ishaDone = 1")
    fun getFullPrayerDaysCount(): Flow<Int>
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    suspend fun getAllWorkoutsSync(): List<WorkoutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM workouts")
    fun getTotalWorkoutMinutes(): Flow<Int?>

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET streakCount = :streak WHERE id = :id")
    suspend fun updateHabitStreak(id: Long, streak: Int)

    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    suspend fun getAllHabitLogsSync(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY dateString DESC")
    suspend fun getLogsForHabitSync(habitId: Long): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLog(habitId: Long, dateString: String)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgressSync(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgressEntity)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}
