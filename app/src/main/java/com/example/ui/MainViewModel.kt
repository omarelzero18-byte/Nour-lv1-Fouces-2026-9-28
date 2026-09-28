package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bennu.*
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.IrtiqaRepository
import com.example.util.NotificationHelper
import com.example.util.PrayerTimesCalculator
import com.example.util.PrayerTimesResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PomodoroState(
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val completedSessions: Int = 0
)

data class WeeklyStats(
    val totalXp: Int = 0,
    val tasksCompleted: Int = 0,
    val workoutsDone: Int = 0,
    val habitCompletions: Int = 0,
    val prayerRatePercent: Int = 0,
    val bestDayArabic: String = "اليوم",
    val bennuCelebrationMsg: String = "أسبوع جميل وبداية قوية مع Nour-lv1! 🚀"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IrtiqaRepository(AppDatabase.getDatabase(application))

    val userProgress: StateFlow<UserProgressEntity> = repository.userProgress
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgressEntity()
        )

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExams: StateFlow<List<TaskEntity>> = repository.upcomingExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayPrayers: StateFlow<PrayerRecordEntity?> = repository.getTodayPrayerRecord()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getTodayHabitLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitLogs: StateFlow<List<HabitLogEntity>> = repository.allHabitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitAnalytics = MutableStateFlow(HabitAnalytics())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    val selectedTab = MutableStateFlow(0) // 0: Home, 1: Study, 2: Prayer, 3: Workout, 4: Habits, 5: Chat
    val bennuSpeech = MutableStateFlow(BennuQuotes.getRandomGreeting())
    val isBennuCelebrating = MutableStateFlow(false)
    val showWeeklyWrapped = MutableStateFlow(false)

    // Inactivity & Dimmed Bennu
    val isBennuDimmed = MutableStateFlow(false)

    // Independent streaks for each of the 4 pillars
    val studyStreak = MutableStateFlow(0)
    val prayerStreak = MutableStateFlow(0)
    val workoutStreak = MutableStateFlow(0)
    val habitsStreak = MutableStateFlow(0)
    val individualPrayerStreaks = MutableStateFlow<Map<String, Int>>(emptyMap())

    // Coordinates (defaults to Cairo, updated via GPS if granted)
    val currentLatitude = MutableStateFlow(PrayerTimesCalculator.DEFAULT_LATITUDE)
    val currentLongitude = MutableStateFlow(PrayerTimesCalculator.DEFAULT_LONGITUDE)

    // Prayer times result state
    val prayerTimes = MutableStateFlow(PrayerTimesCalculator.calculate())

    // Pomodoro Timer State
    val pomodoroState = MutableStateFlow(PomodoroState())
    private var pomodoroJob: Job? = null

    // Weekly Wrapped state
    val weeklyStats = MutableStateFlow(WeeklyStats())

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            refreshStreaksAndInactivity()
            refreshHabitAnalytics()
            updatePrayerTimes()
            // Minute-by-minute live ticker for prayer times and countdown
            startPrayerTicker()
        }
    }

    fun refreshHabitAnalytics() {
        viewModelScope.launch {
            val analytics = repository.calculateHabitAnalytics()
            habitAnalytics.value = analytics
        }
    }

    private fun startPrayerTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                updatePrayerTimes()
            }
        }
    }

    fun updatePrayerTimes(
        latitude: Double = currentLatitude.value,
        longitude: Double = currentLongitude.value
    ) {
        val result = PrayerTimesCalculator.calculate(latitude = latitude, longitude = longitude)
        prayerTimes.value = result
        try {
            NotificationHelper.createNotificationChannel(getApplication())
            NotificationHelper.scheduleDailyAlarms(getApplication(), result, userProgress.value.userName)
        } catch (_: Exception) {}
    }

    fun setLocationCoordinates(latitude: Double, longitude: Double) {
        currentLatitude.value = latitude
        currentLongitude.value = longitude
        updatePrayerTimes(latitude, longitude)
    }

    fun refreshStreaksAndInactivity() {
        viewModelScope.launch {
            studyStreak.value = repository.getStudyStreak()
            prayerStreak.value = repository.getPrayerStreak()
            workoutStreak.value = repository.getWorkoutStreak()
            habitsStreak.value = repository.getHabitsStreak()
            individualPrayerStreaks.value = repository.getIndividualPrayerStreaks()

            // Check inactivity (3+ days)
            val progress = repository.userProgress.firstOrNull()
            val lastDate = progress?.lastActiveDate ?: ""
            val days = repository.getDaysBetween(lastDate, repository.getTodayDateString())
            val wasDimmed = isBennuDimmed.value
            val isNowDimmed = days >= 3
            isBennuDimmed.value = isNowDimmed

            if (wasDimmed && !isNowDimmed) {
                triggerCelebration("وحشتني يا بطل! نورت مكانك وNour-lv1 رجع بريقه ونوره كامل بوجودك.. يلا نكمل سوا! 🌟")
            }
        }
    }

    fun selectTab(index: Int) {
        selectedTab.value = index
    }

    fun triggerCelebration(customMsg: String? = null) {
        viewModelScope.launch {
            isBennuCelebrating.value = true
            bennuSpeech.value = customMsg ?: BennuQuotes.getRandomCelebration()
            delay(2500)
            isBennuCelebrating.value = false
        }
    }

    fun onBennuTapped() {
        if (isBennuDimmed.value) {
            triggerCelebration("أنا مستنيك يا بطل! كل خطوة صغيرة النهارده هترجع بريقي كامل من تاني 🌟🦅")
            return
        }
        val quotes = listOf(
            "منور يا فنان! أنا شايفك مركز وشغال تمام 🦅",
            "يلا نكسر الدنيا النهارده! الركائز الأربعة مستنياك 💪",
            "أنا فخور بيك جداً! كل خطوة بتفرق والله 🌟",
            "ولا تشيل هم الامتحانات، المهم نبدأ سوا دلوقتي! 🔥",
            "فاكر مواقيت الصلاة؟ بتديك طاقة وبركة مفيش زيها 🕌"
        )
        triggerCelebration(quotes.random())
    }

    // --- Actions with Anti-Farming & Streak Updates ---
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            repository.toggleTaskCompletion(task)
            refreshStreaksAndInactivity()
            if (willBeCompleted) {
                triggerCelebration("الله عليك! خلصت مهمة '${task.title}' وأخدت +${task.xpAwarded} XP! 🔥")
            }
        }
    }

    fun addTask(
        title: String,
        subjectId: Long = 0,
        priority: String = "NORMAL",
        isExam: Boolean = false,
        dueDateMillis: Long = System.currentTimeMillis() + 86400000L
    ) {
        viewModelScope.launch {
            repository.addTask(
                title = title,
                subjectId = subjectId,
                dueDateMillis = dueDateMillis,
                priority = priority,
                isExam = isExam
            )
            refreshStreaksAndInactivity()
            triggerCelebration(if (isExam) "سجلتلك الامتحان في وضع الطوارئ.. ركز وهتعدي يا كبير! 📝" else "أضفت لك المهمة في قائمة المذاكرة 📚")
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            refreshStreaksAndInactivity()
        }
    }

    fun togglePrayer(prayerKey: String) {
        viewModelScope.launch {
            val isNowDone = repository.togglePrayer(prayerKey)
            refreshStreaksAndInactivity()
            if (isNowDone) {
                triggerCelebration("تقبل الله يا بطل! صلاة في وقتها بتنوّر قلبك ويومك 🕌 (+15 XP)")
            }
        }
    }

    fun addWorkout(title: String, type: String, durationMinutes: Int, setsReps: String = "") {
        viewModelScope.launch {
            repository.addWorkout(title, type, durationMinutes, setsReps)
            refreshStreaksAndInactivity()
            triggerCelebration("عاش يا وحش! تمرين حديد ونشاط يفتح النفس 💪 (+30 XP)")
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
            refreshStreaksAndInactivity()
        }
    }

    fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, isDone)
            refreshStreaksAndInactivity()
            refreshHabitAnalytics()
            if (!isDone) {
                triggerCelebration("عاش على الالتزام بالعادة! خطوة صغيرة بتعمل فرق جبار 🌱 (+10 XP)")
            }
        }
    }

    fun addHabit(title: String, icon: String) {
        viewModelScope.launch {
            repository.addHabit(title, icon)
            refreshStreaksAndInactivity()
            refreshHabitAnalytics()
            triggerCelebration("أضفتلك عادة جديدة! يلا نلتزم بيها كل يوم ✨")
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            refreshStreaksAndInactivity()
            refreshHabitAnalytics()
            triggerCelebration("تم حذف العادة بنجاح 🗑️")
        }
    }

    // Pomodoro controls
    fun togglePomodoro() {
        if (pomodoroState.value.isRunning) {
            pomodoroJob?.cancel()
            pomodoroState.value = pomodoroState.value.copy(isRunning = false)
        } else {
            pomodoroState.value = pomodoroState.value.copy(isRunning = true)
            startPomodoroTicker()
        }
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        val defaultSec = if (pomodoroState.value.isBreak) 5 * 60 else 25 * 60
        pomodoroState.value = pomodoroState.value.copy(
            isRunning = false,
            totalSeconds = defaultSec,
            remainingSeconds = defaultSec
        )
    }

    private fun startPomodoroTicker() {
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (pomodoroState.value.isRunning && pomodoroState.value.remainingSeconds > 0) {
                delay(1000)
                val newRemaining = pomodoroState.value.remainingSeconds - 1
                pomodoroState.value = pomodoroState.value.copy(remainingSeconds = newRemaining)
            }
            if (pomodoroState.value.remainingSeconds <= 0) {
                val wasBreak = pomodoroState.value.isBreak
                if (!wasBreak) {
                    repository.logPomodoroCompleted(25)
                    refreshStreaksAndInactivity()
                    triggerCelebration("بطل والله! كملت جلسة بومودورو 25 دقيقة وأخدت +25 XP! ⏱️ خذلك بريك 5 دقايق")
                    pomodoroState.value = PomodoroState(
                        isRunning = false,
                        isBreak = true,
                        totalSeconds = 5 * 60,
                        remainingSeconds = 5 * 60,
                        completedSessions = pomodoroState.value.completedSessions + 1
                    )
                } else {
                    triggerCelebration("خلص البريك! جاهز نبدأ جلسة جديدة يا فنان؟ 📚")
                    pomodoroState.value = PomodoroState(
                        isRunning = false,
                        isBreak = false,
                        totalSeconds = 25 * 60,
                        remainingSeconds = 25 * 60,
                        completedSessions = pomodoroState.value.completedSessions
                    )
                }
            }
        }
    }

    // Bennu Chat
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            repository.saveChatMessage(sender = "user", text = userText)

            // Gather context
            val currentTasks = allTasks.value.filter { !it.isCompleted }.take(3).joinToString { it.title }
            val nextPray = prayerTimes.value.nextPrayer?.nameArabic ?: ""
            val context = "المستخدم لديه مهام: [$currentTasks] والصلاة القادمة: $nextPray"

            val (botReply, parsedIntent) = BennuAIAssistant.chat(userText, context)

            // If intent was detected, execute corresponding action
            when (parsedIntent) {
                is ParsedIntent.AddTask -> {
                    repository.addTask(
                        title = parsedIntent.title,
                        priority = parsedIntent.priority,
                        isExam = parsedIntent.isExam
                    )
                    refreshStreaksAndInactivity()
                }
                is ParsedIntent.AddHabit -> {
                    repository.addHabit(parsedIntent.title, parsedIntent.icon)
                    refreshStreaksAndInactivity()
                }
                is ParsedIntent.AddWorkout -> {
                    repository.addWorkout(parsedIntent.title, parsedIntent.type, parsedIntent.durationMinutes)
                    refreshStreaksAndInactivity()
                }
                null, is ParsedIntent.GeneralChat -> {}
            }

            repository.saveChatMessage(sender = "bennu", text = botReply)
            triggerCelebration(botReply)
        }
    }

    // Real Weekly Wrapped calculation (Last 7 Days)
    fun openWeeklyWrapped() {
        viewModelScope.launch {
            val stats = repository.calculateRealWeeklyStats()
            weeklyStats.value = stats
            showWeeklyWrapped.value = true
        }
    }

    fun closeWeeklyWrapped() {
        showWeeklyWrapped.value = false
    }

    // Onboarding completion with GPS coordinates
    fun completeOnboarding(
        userName: String,
        selectedHabits: List<Pair<String, String>>,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (latitude != null && longitude != null) {
            currentLatitude.value = latitude
            currentLongitude.value = longitude
        }
        viewModelScope.launch {
            repository.completeOnboarding(userName, selectedHabits)
            updatePrayerTimes()
            refreshStreaksAndInactivity()
            triggerCelebration("أهلاً بيك يا $userName في Fucos! بيضة Nour بدأت تنور وبدأت الرحلة! 🐣✨")
        }
    }
}
