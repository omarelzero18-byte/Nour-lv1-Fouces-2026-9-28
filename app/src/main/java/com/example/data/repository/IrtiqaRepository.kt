package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.ui.WeeklyStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class IrtiqaRepository(private val db: AppDatabase) {

    private val studyDao = db.studyDao()
    private val prayerDao = db.prayerDao()
    private val workoutDao = db.workoutDao()
    private val habitDao = db.habitDao()
    private val progressDao = db.userProgressDao()
    private val chatDao = db.chatDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTodayDateString(): String = dateFormat.format(Date())

    fun getDaysAgoDateString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    fun getYesterdayDateString(): String = getDaysAgoDateString(1)

    fun getDaysBetween(dateStr1: String, dateStr2: String): Int {
        if (dateStr1.isBlank() || dateStr2.isBlank()) return 999
        return try {
            val d1 = dateFormat.parse(dateStr1)
            val d2 = dateFormat.parse(dateStr2)
            if (d1 != null && d2 != null) {
                val diff = Math.abs(d2.time - d1.time)
                (diff / (24 * 60 * 60 * 1000)).toInt()
            } else 999
        } catch (e: Exception) {
            999
        }
    }

    // --- Study Pillar ---
    val allSubjects: Flow<List<SubjectEntity>> = studyDao.getAllSubjects()
    val allTasks: Flow<List<TaskEntity>> = studyDao.getAllTasks()
    val upcomingExams: Flow<List<TaskEntity>> = studyDao.getUpcomingExams()

    suspend fun addSubject(name: String, colorHex: String, icon: String = "book") = withContext(Dispatchers.IO) {
        studyDao.insertSubject(SubjectEntity(name = name, colorHex = colorHex, icon = icon))
    }

    suspend fun addTask(
        title: String,
        subjectId: Long = 0,
        dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
        priority: String = "NORMAL",
        isExam: Boolean = false,
        examDateMillis: Long = 0
    ) = withContext(Dispatchers.IO) {
        studyDao.insertTask(
            TaskEntity(
                title = title,
                subjectId = subjectId,
                dueDateMillis = dueDateMillis,
                priority = priority,
                isExam = isExam,
                examDateMillis = if (isExam && examDateMillis == 0L) dueDateMillis else examDateMillis
            )
        )
    }

    // Anti-farming: strictly award on check, deduct identical XP on uncheck
    suspend fun toggleTaskCompletion(task: TaskEntity) = withContext(Dispatchers.IO) {
        val willBeCompleted = !task.isCompleted
        studyDao.updateTask(task.copy(isCompleted = willBeCompleted))
        if (willBeCompleted) {
            awardXp(task.xpAwarded)
        } else {
            deductXp(task.xpAwarded)
        }
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteTask(id)
    }

    // --- Prayer Pillar ---
    fun getTodayPrayerRecord(): Flow<PrayerRecordEntity?> {
        return prayerDao.getPrayerRecord(getTodayDateString())
    }

    val allPrayerRecords: Flow<List<PrayerRecordEntity>> = prayerDao.getAllPrayerRecords()

    // Anti-farming: strictly award +15 on check, deduct -15 on uncheck
    suspend fun togglePrayer(prayerKey: String): Boolean = withContext(Dispatchers.IO) {
        val today = getTodayDateString()
        val current = prayerDao.getPrayerRecordSync(today) ?: PrayerRecordEntity(dateString = today)
        val wasDone = when (prayerKey) {
            "fajr" -> current.fajrDone
            "dhuhr" -> current.dhuhrDone
            "asr" -> current.asrDone
            "maghrib" -> current.maghribDone
            "isha" -> current.ishaDone
            else -> false
        }
        val isNowDone = !wasDone
        val updated = when (prayerKey) {
            "fajr" -> current.copy(fajrDone = isNowDone)
            "dhuhr" -> current.copy(dhuhrDone = isNowDone)
            "asr" -> current.copy(asrDone = isNowDone)
            "maghrib" -> current.copy(maghribDone = isNowDone)
            "isha" -> current.copy(ishaDone = isNowDone)
            else -> current
        }
        prayerDao.insertOrUpdate(updated)

        if (isNowDone) {
            awardXp(15)
        } else {
            deductXp(15)
        }
        isNowDone
    }

    // --- Workout Pillar ---
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun addWorkout(
        title: String,
        type: String,
        durationMinutes: Int,
        setsReps: String = "",
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        workoutDao.insertWorkout(
            WorkoutEntity(
                dateString = getTodayDateString(),
                title = title,
                type = type,
                durationMinutes = durationMinutes,
                setsReps = setsReps,
                notes = notes,
                xpEarned = 30
            )
        )
        awardXp(30)
    }

    suspend fun deleteWorkout(id: Long) = withContext(Dispatchers.IO) {
        workoutDao.deleteWorkout(id)
        deductXp(30) // Deduct associated workout XP if deleted
    }

    // --- Habits Pillar ---
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    fun getTodayHabitLogs(): Flow<List<HabitLogEntity>> = habitDao.getLogsForDate(getTodayDateString())
    val allHabitLogs: Flow<List<HabitLogEntity>> = habitDao.getAllHabitLogs()

    suspend fun addHabit(title: String, icon: String = "✨") = withContext(Dispatchers.IO) {
        habitDao.insertHabit(HabitEntity(title = title, icon = icon))
    }

    // Anti-farming: strictly award on check, deduct on uncheck, and recalculate habit.streakCount
    suspend fun toggleHabitLog(habitId: Long, isDone: Boolean) = withContext(Dispatchers.IO) {
        val today = getTodayDateString()
        if (isDone) {
            habitDao.deleteLog(habitId, today)
            deductXp(10)
        } else {
            habitDao.insertLog(HabitLogEntity(habitId = habitId, dateString = today))
            awardXp(10)
        }
        updateHabitStreak(habitId)
    }

    suspend fun updateHabitStreak(habitId: Long) = withContext(Dispatchers.IO) {
        val logs = habitDao.getLogsForHabitSync(habitId)
        val dates = logs.map { it.dateString }.toSet()
        val streak = calculateConsecutiveStreak(dates)
        habitDao.updateHabitStreak(habitId, streak)
    }

    suspend fun deleteHabit(habitId: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabit(habitId)
        habitDao.deleteLogsForHabit(habitId)
    }

    suspend fun calculateHabitAnalytics(): HabitAnalytics = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllHabitsSync()
        if (habits.isEmpty()) return@withContext HabitAnalytics()

        val today = getTodayDateString()
        val last7Days = (0..6).map { getDaysAgoDateString(it) }.toSet()
        val last30Days = (0..29).map { getDaysAgoDateString(it) }.toSet()

        val allLogs = habitDao.getAllHabitLogsSync()
        val logsLast7Days = allLogs.filter { it.dateString in last7Days }
        val logsLast30Days = allLogs.filter { it.dateString in last30Days }

        // Overall progress score: total completions over last 7 days / (habits.size * 7)
        val totalCompletions7Days = logsLast7Days.size
        val totalPossible7Days = habits.size * 7
        val overallProgressPercent = if (totalPossible7Days > 0) {
            ((totalCompletions7Days.toFloat() / totalPossible7Days) * 100f).toInt().coerceIn(0, 100)
        } else 0

        // Per-habit statistics
        val habitPerformances = habits.map { habit ->
            val habitLogsIn7 = logsLast7Days.count { it.habitId == habit.id }
            val rate = ((habitLogsIn7.toFloat() / 7f) * 100f).toInt().coerceIn(0, 100)
            HabitPerformance(
                habit = habit,
                streak = habit.streakCount,
                completionsLast7Days = habitLogsIn7,
                completionRatePercent = rate
            )
        }

        // Top consistently formed habits (top 3-5 by streak then completions in 7 days)
        val topConsistent = habitPerformances
            .sortedWith(compareByDescending<HabitPerformance> { it.streak }.thenByDescending { it.completionsLast7Days })
            .take(5)

        // Most neglected habits (top 3-5 over last 14-30 days)
        val logsByHabit = allLogs.groupBy { it.habitId }
        val neglectedList = habits.map { habit ->
            val hLogs = logsByHabit[habit.id] ?: emptyList()
            val completions30 = logsLast30Days.count { it.habitId == habit.id }

            val latestDate = hLogs.maxByOrNull { it.dateString }?.dateString
            val daysSince = if (latestDate != null) {
                getDaysBetween(latestDate, today)
            } else {
                val createdDays = ((System.currentTimeMillis() - habit.createdAtMillis) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(1, 30)
                createdDays
            }

            val label = when {
                daysSince >= 14 -> "إهمال حرج ⚠️"
                daysSince >= 7 -> "مهملة أسبوعاً ⌛"
                daysSince >= 3 -> "تحتاج انتباه 🔔"
                else -> "تحتاج استمرارية 🌱"
            }

            HabitNeglectInfo(
                habit = habit,
                daysSinceLastCompleted = daysSince,
                completionsLast30Days = completions30,
                statusLabel = label
            )
        }
        .sortedWith(compareByDescending<HabitNeglectInfo> { it.daysSinceLastCompleted }.thenBy { it.completionsLast30Days })
        .take(5)

        // Neglect alert logic from Nour-lv1
        val mostNeglected = neglectedList.firstOrNull { it.daysSinceLastCompleted >= 2 }
        val neglectAlert = if (mostNeglected != null) {
            val habit = mostNeglected.habit
            val days = mostNeglected.daysSinceLastCompleted
            val msg = "ملاحظ إن عادة '${habit.icon} ${habit.title}' متوقفة بقالها $days أيام.. Nour-lv1 بيفكرك: خطوة واحدة صغيرة النهاردة كافية ترجعك للمسار الصحيح! 🌟 جاهز ننفذها دلوقتي؟"
            NeglectAlert(
                habitTitle = habit.title,
                habitIcon = habit.icon,
                daysNeglected = days,
                motivationalPrompt = msg
            )
        } else null

        HabitAnalytics(
            overallProgressPercent = overallProgressPercent,
            totalCompletions7Days = totalCompletions7Days,
            topConsistentHabits = topConsistent,
            mostNeglectedHabits = neglectedList,
            neglectAlert = neglectAlert
        )
    }

    // --- Pomodoro Completion ---
    suspend fun logPomodoroCompleted(durationMinutes: Int) = withContext(Dispatchers.IO) {
        val current = progressDao.getProgressSync() ?: UserProgressEntity()
        awardXp(25)
        progressDao.insertOrUpdate(
            current.copy(
                totalStudyMinutes = current.totalStudyMinutes + durationMinutes
            )
        )
    }

    // --- User Progress, XP & Anti-Farming ---
    val userProgress: Flow<UserProgressEntity?> = progressDao.getProgress()

    suspend fun awardXp(amount: Int) = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext
        val current = progressDao.getProgressSync() ?: UserProgressEntity()
        val newXp = (current.totalXp + amount).coerceAtLeast(0)
        val newLevel = calculateLevel(newXp)

        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        // Strict streak breaking logic:
        // If last active was today: keep streak
        // If last active was yesterday: increment streak by 1
        // If last active was before yesterday: streak breaks and resets to 1!
        val newStreak = when {
            current.lastActiveDate.isBlank() -> 1
            current.lastActiveDate == today -> current.streakDays
            current.lastActiveDate == yesterday -> current.streakDays + 1
            else -> 1 // Was older than yesterday, broken!
        }

        progressDao.insertOrUpdate(
            current.copy(
                totalXp = newXp,
                level = newLevel,
                streakDays = newStreak,
                lastActiveDate = today
            )
        )
    }

    suspend fun deductXp(amount: Int) = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext
        val current = progressDao.getProgressSync() ?: UserProgressEntity()
        val newXp = (current.totalXp - amount).coerceAtLeast(0)
        val newLevel = calculateLevel(newXp)
        progressDao.insertOrUpdate(
            current.copy(
                totalXp = newXp,
                level = newLevel
            )
        )
    }

    private fun calculateLevel(xp: Int): Int = when {
        xp < 100 -> 1
        xp < 300 -> 2
        xp < 700 -> 3
        else -> 4
    }

    // Check if streak was broken due to inactivity on app start
    suspend fun checkAndRefreshStreak() = withContext(Dispatchers.IO) {
        val current = progressDao.getProgressSync() ?: return@withContext
        if (current.lastActiveDate.isNotBlank()) {
            val days = getDaysBetween(current.lastActiveDate, getTodayDateString())
            if (days > 1 && current.streakDays > 0) {
                // Streak broken because more than 1 full day has passed without activity!
                progressDao.insertOrUpdate(current.copy(streakDays = 0))
            }
        }
    }

    // --- Consecutive Day Streak Algorithm ---
    fun calculateConsecutiveStreak(activeDates: Set<String>): Int {
        if (activeDates.isEmpty()) return 0
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        val startOffset = when {
            activeDates.contains(today) -> 0
            activeDates.contains(yesterday) -> 1
            else -> return 0 // Streak broken
        }

        var streak = 0
        var offset = startOffset
        while (true) {
            val checkDate = getDaysAgoDateString(offset)
            if (activeDates.contains(checkDate)) {
                streak++
                offset++
            } else {
                break
            }
        }
        return streak
    }

    // --- Independent Streaks for the 4 Pillars ---
    suspend fun getStudyStreak(): Int = withContext(Dispatchers.IO) {
        val tasks = studyDao.getAllTasksSync()
        // If there are completed tasks today, count today
        val activeDates = mutableSetOf<String>()
        if (tasks.any { it.isCompleted }) {
            activeDates.add(getTodayDateString())
        }
        // In a real app, completion timestamps can be logged; here we check active dates
        val progress = progressDao.getProgressSync()
        if (progress != null && progress.totalStudyMinutes > 0 && progress.lastActiveDate.isNotBlank()) {
            activeDates.add(progress.lastActiveDate)
        }
        calculateConsecutiveStreak(activeDates)
    }

    suspend fun getPrayerStreak(): Int = withContext(Dispatchers.IO) {
        val records = prayerDao.getAllPrayerRecordsSync()
        val datesWithAtLeastOnePrayer = records.filter {
            it.fajrDone || it.dhuhrDone || it.asrDone || it.maghribDone || it.ishaDone
        }.map { it.dateString }.toSet()
        calculateConsecutiveStreak(datesWithAtLeastOnePrayer)
    }

    suspend fun getWorkoutStreak(): Int = withContext(Dispatchers.IO) {
        val workouts = workoutDao.getAllWorkoutsSync()
        val workoutDates = workouts.map { it.dateString }.toSet()
        calculateConsecutiveStreak(workoutDates)
    }

    suspend fun getHabitsStreak(): Int = withContext(Dispatchers.IO) {
        val logs = habitDao.getAllHabitLogsSync()
        val habitDates = logs.map { it.dateString }.toSet()
        calculateConsecutiveStreak(habitDates)
    }

    // Independent streaks for each of the 5 daily prayers
    suspend fun getIndividualPrayerStreaks(): Map<String, Int> = withContext(Dispatchers.IO) {
        val records = prayerDao.getAllPrayerRecordsSync()
        val fajrDates = records.filter { it.fajrDone }.map { it.dateString }.toSet()
        val dhuhrDates = records.filter { it.dhuhrDone }.map { it.dateString }.toSet()
        val asrDates = records.filter { it.asrDone }.map { it.dateString }.toSet()
        val maghribDates = records.filter { it.maghribDone }.map { it.dateString }.toSet()
        val ishaDates = records.filter { it.ishaDone }.map { it.dateString }.toSet()

        mapOf(
            "fajr" to calculateConsecutiveStreak(fajrDates),
            "dhuhr" to calculateConsecutiveStreak(dhuhrDates),
            "asr" to calculateConsecutiveStreak(asrDates),
            "maghrib" to calculateConsecutiveStreak(maghribDates),
            "isha" to calculateConsecutiveStreak(ishaDates)
        )
    }

    // --- Real Weekly Stats (Last 7 Days) ---
    suspend fun calculateRealWeeklyStats(): WeeklyStats = withContext(Dispatchers.IO) {
        val last7Days = (0..6).map { getDaysAgoDateString(it) }.toSet()
        val progress = progressDao.getProgressSync() ?: UserProgressEntity()

        // 1. Tasks completed
        val allTasks = studyDao.getAllTasksSync()
        val completedTasksCount = allTasks.count { it.isCompleted }

        // 2. Workouts in last 7 days
        val allWorkouts = workoutDao.getAllWorkoutsSync()
        val workoutsInWeek = allWorkouts.filter { it.dateString in last7Days }
        val workoutsCount = workoutsInWeek.size

        // 3. Habits completed in last 7 days
        val allHabitLogs = habitDao.getAllHabitLogsSync()
        val habitsInWeek = allHabitLogs.filter { it.dateString in last7Days }
        val habitCompletionsCount = habitsInWeek.size

        // 4. Prayers completed in last 7 days (out of 7 * 5 = 35 prayers)
        val allPrayerRecords = prayerDao.getAllPrayerRecordsSync()
        val prayersInWeek = allPrayerRecords.filter { it.dateString in last7Days }
        var totalPrayersDone = 0
        prayersInWeek.forEach { record ->
            if (record.fajrDone) totalPrayersDone++
            if (record.dhuhrDone) totalPrayersDone++
            if (record.asrDone) totalPrayersDone++
            if (record.maghribDone) totalPrayersDone++
            if (record.ishaDone) totalPrayersDone++
        }
        val prayerRate = ((totalPrayersDone.toFloat() / 35f) * 100f).toInt().coerceIn(0, 100)

        // 5. Best day in the last 7 days
        val dayScores = mutableMapOf<String, Int>()
        for (dayStr in last7Days) {
            val prayRecord = prayersInWeek.firstOrNull { it.dateString == dayStr }
            val prayScore = (if (prayRecord?.fajrDone == true) 1 else 0) +
                    (if (prayRecord?.dhuhrDone == true) 1 else 0) +
                    (if (prayRecord?.asrDone == true) 1 else 0) +
                    (if (prayRecord?.maghribDone == true) 1 else 0) +
                    (if (prayRecord?.ishaDone == true) 1 else 0)

            val workoutScore = workoutsInWeek.count { it.dateString == dayStr }
            val habitScore = habitsInWeek.count { it.dateString == dayStr }
            dayScores[dayStr] = prayScore + workoutScore + habitScore
        }

        val bestDayDateStr = dayScores.maxByOrNull { it.value }?.key ?: getTodayDateString()
        val bestDayArabic = getArabicDayName(bestDayDateStr)

        val celebrationMsg = when {
            totalPrayersDone >= 25 && workoutsCount >= 3 ->
                "أداء أسطوري يا بطل! انتظام الصلاة والتمرين الأسبوع ده كان فوق الممتاز وNour-lv1 فخور بيك جداً! 🌟🔥"
            completedTasksCount >= 4 || habitCompletionsCount >= 10 ->
                "عاش يا فنان! كمية الإنجاز والانضباط الأسبوع ده بتصنع فارق حقيقي في حياتك ودراستك! 🦅✨"
            else ->
                "أسبوع جميل وبداية قوية! كل خطوة بتعملها بتكبر مستواك مع Nour-lv1.. الأسبوع الجاي هنكسر أرقام قياسية جديدة! 🚀"
        }

        WeeklyStats(
            totalXp = progress.totalXp,
            tasksCompleted = completedTasksCount,
            workoutsDone = workoutsCount,
            habitCompletions = habitCompletionsCount,
            prayerRatePercent = prayerRate,
            bestDayArabic = bestDayArabic,
            bennuCelebrationMsg = celebrationMsg
        )
    }

    private fun getArabicDayName(dateStr: String): String {
        return try {
            val d = dateFormat.parse(dateStr) ?: return "اليوم"
            val cal = Calendar.getInstance().apply { time = d }
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "السبت"
                Calendar.SUNDAY -> "الأحد"
                Calendar.MONDAY -> "الإثنين"
                Calendar.TUESDAY -> "الثلاثاء"
                Calendar.WEDNESDAY -> "الأربعاء"
                Calendar.THURSDAY -> "الخميس"
                Calendar.FRIDAY -> "الجمعة"
                else -> "اليوم"
            }
        } catch (e: Exception) {
            "اليوم"
        }
    }

    // --- Onboarding ---
    suspend fun completeOnboarding(
        name: String,
        starterHabits: List<Pair<String, String>>
    ) = withContext(Dispatchers.IO) {
        val current = progressDao.getProgressSync() ?: UserProgressEntity()
        progressDao.insertOrUpdate(
            current.copy(
                userName = name.ifBlank { "بطل Fucos" },
                onboardingCompleted = true,
                totalXp = current.totalXp + 30, // First win welcome bonus!
                lastActiveDate = getTodayDateString(),
                streakDays = 1
            )
        )

        // Seed default subjects
        studyDao.insertSubject(SubjectEntity(name = "الفيزياء", colorHex = "#38BDF8", icon = "book"))
        studyDao.insertSubject(SubjectEntity(name = "الرياضيات", colorHex = "#F59E0B", icon = "calculate"))
        studyDao.insertSubject(SubjectEntity(name = "اللغة العربية", colorHex = "#10B981", icon = "menu_book"))
        studyDao.insertSubject(SubjectEntity(name = "اللغة الإنجليزية", colorHex = "#A855F7", icon = "language"))

        // Seed starter habits selected
        for ((title, icon) in starterHabits) {
            habitDao.insertHabit(HabitEntity(title = title, icon = icon))
        }

        // Add starter task for fast 60s win
        studyDao.insertTask(
            TaskEntity(
                title = "أول خطوة: خطط لأول جلسة مذاكرة وتركيز مع Nour-lv1 ✨",
                subjectId = 1,
                priority = "URGENT",
                dueDateMillis = System.currentTimeMillis() + 86400000L
            )
        )
    }

    // --- Chat Messages ---
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun saveChatMessage(sender: String, text: String, actionTaken: String? = null) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(
            ChatMessageEntity(
                sender = sender,
                text = text,
                actionTaken = actionTaken
            )
        )
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        val progress = progressDao.getProgressSync()
        if (progress == null) {
            progressDao.insertOrUpdate(UserProgressEntity(lastActiveDate = getTodayDateString()))
        } else {
            checkAndRefreshStreak()
        }
    }
}
