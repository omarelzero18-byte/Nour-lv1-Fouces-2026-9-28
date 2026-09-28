package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bennu.BennuCharacter
import com.example.bennu.BennuStage
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val exams by viewModel.upcomingExams.collectAsStateWithLifecycle()
    val prayers by viewModel.todayPrayers.collectAsStateWithLifecycle()
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val bennuSpeech by viewModel.bennuSpeech.collectAsStateWithLifecycle()
    val isCelebrating by viewModel.isBennuCelebrating.collectAsStateWithLifecycle()
    val isBennuDimmed by viewModel.isBennuDimmed.collectAsStateWithLifecycle()

    val studyStreak by viewModel.studyStreak.collectAsStateWithLifecycle()
    val prayerStreak by viewModel.prayerStreak.collectAsStateWithLifecycle()
    val workoutStreak by viewModel.workoutStreak.collectAsStateWithLifecycle()
    val habitsStreak by viewModel.habitsStreak.collectAsStateWithLifecycle()

    val currentStage = BennuStage.fromXp(progress.totalXp)
    val nextStageXp = currentStage.maxXp
    val currentLevelProgress = (progress.totalXp - currentStage.minXp).toFloat() / (currentStage.maxXp - currentStage.minXp).coerceAtLeast(1)

    val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US) }
    val last7DaysSet = remember {
        (0..6).map {
            val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -it) }
            dateFormat.format(c.time)
        }.toSet()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Top Bar: User Welcome, Streak, XP Progress ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MidnightCardBorder, Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "أهلاً يا ${progress.userName} 🌟",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "«ارتقِ كل يوم خطوة»",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGold
                            )
                        }

                        // Streak Flame
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x29FF9100))
                                .border(1.dp, Color(0x66FF9100), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progress.streakDays} أيام",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Unified XP Bar for Bennu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentStage.titleArabic,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = BennuGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(مستوى ${currentStage.level})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "${progress.totalXp} / $nextStageXp XP",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { currentLevelProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = BennuGold,
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }

        // --- Mascot Hero Card: Bennu Alive in Center ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(24.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.radialGradient(listOf(BennuGold.copy(alpha = 0.4f), Color.Transparent))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onBennuTapped() }
                    .testTag("bennu_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BennuCharacter(
                        stage = currentStage,
                        size = 130.dp,
                        isCelebrating = isCelebrating,
                        isDimmed = isBennuDimmed,
                        onClick = { viewModel.onBennuTapped() }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Bennu Speech Bubble
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                                .background(Color(0xFF1B243B))
                                .border(1.dp, MidnightCardBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (isBennuDimmed) "Nour-lv1 مستنيك بهدوء ترجع تنور الركائز وتكبروا سوا 🌟💤" else bennuSpeech,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "اضغط على Nour-lv1 عشان تتكلم معاه ✨",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // --- Emergency Mode Exam Banner (if upcoming exam exists) ---
        if (exams.isNotEmpty()) {
            val nextExam = exams.first()
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0F17)),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(UrgentRose, Color.Transparent))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTab(1) } // Go to study tab
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🚨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "وضع الطوارئ: ${nextExam.title}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = UrgentRose
                            )
                            Text(
                                text = "الامتحان قرب! Nour-lv1 معاك: ركز في البومودورو وهتكسر الدنيا 🔥",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        IconButton(onClick = { viewModel.selectTab(1) }) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "ابدأ بومودورو", tint = UrgentRose)
                        }
                    }
                }
            }
        }

        // --- Weekly Celebration Wrapped Banner ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1735)),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openWeeklyWrapped() }
                    .testTag("weekly_wrapped_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ملخصك الأسبوعي مع Nour-lv1 ✨",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF0ABFC)
                            )
                            Text(
                                text = "شوف إنجازاتك وأكتر يوم كنت فيه بطل!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.openWeeklyWrapped() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "افتح", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Section Title: The 4 Pillars ---
        item {
            Text(
                text = "الركائز الأربعة ليومك ⚖️",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- Pillar 1: Study (المذاكرة) ---
        item {
            val pendingTasks = tasks.filter { !it.isCompleted }
            PillarSummaryCard(
                icon = "📚",
                title = "المذاكرة",
                subtitle = if (pendingTasks.isEmpty()) "كل الواجبات مكتملة يا عبقري! 🌟" else "عندك ${pendingTasks.size} مهام تنتظر إنجازك",
                accentColor = StudyCyan,
                streak = studyStreak,
                actionLabel = "قائمة المذاكرة",
                onAction = { viewModel.selectTab(1) }
            ) {
                if (pendingTasks.isNotEmpty()) {
                    val firstTask = pendingTasks.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightSurfaceVariant)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { viewModel.toggleTask(firstTask) },
                            colors = CheckboxDefaults.colors(checkedColor = StudyCyan, uncheckedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = firstTask.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // --- Pillar 2: Prayer (الصلاة) ---
        item {
            val nextPray = prayerTimes.nextPrayer
            val nextTimeStr = nextPray?.formattedTime ?: ""
            val nextName = nextPray?.nameArabic ?: ""
            val minutesLeft = prayerTimes.minutesUntilNext

            PillarSummaryCard(
                icon = "🕌",
                title = "الصلاة",
                subtitle = "القادمة: $nextName ($nextTimeStr) — باقي $minutesLeft دقيقة",
                accentColor = PrayerGreen,
                streak = prayerStreak,
                actionLabel = "جدول الصلوات",
                onAction = { viewModel.selectTab(2) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrayerQuickBadge(name = "الفجر", done = prayers?.fajrDone == true) { viewModel.togglePrayer("fajr") }
                    PrayerQuickBadge(name = "الظهر", done = prayers?.dhuhrDone == true) { viewModel.togglePrayer("dhuhr") }
                    PrayerQuickBadge(name = "العصر", done = prayers?.asrDone == true) { viewModel.togglePrayer("asr") }
                    PrayerQuickBadge(name = "المغرب", done = prayers?.maghribDone == true) { viewModel.togglePrayer("maghrib") }
                    PrayerQuickBadge(name = "العشاء", done = prayers?.ishaDone == true) { viewModel.togglePrayer("isha") }
                }
            }
        }

        // --- Pillar 3: Workout (التمرين) ---
        item {
            val thisWeekWorkouts = workouts.filter { it.dateString in last7DaysSet }
            val weekMinutes = thisWeekWorkouts.sumOf { it.durationMinutes }
            PillarSummaryCard(
                icon = "🏋️",
                title = "التمرين والجيم",
                subtitle = if (thisWeekWorkouts.isEmpty()) "ابدأ تمرين منزلي 15 دقيقة خفيف ونشط مخك 💪" else "سجلت ${thisWeekWorkouts.size} تمارين (${weekMinutes} دقيقة) هذا الأسبوع!",
                accentColor = WorkoutOrange,
                streak = workoutStreak,
                actionLabel = "سجل تمرين",
                onAction = { viewModel.selectTab(3) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addWorkout("تمرين منزلي 15 دقيقة", "HOME", 15, "ضغط وبطن") },
                        colors = ButtonDefaults.buttonColors(containerColor = MidnightSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "منزلي 15د 🏠", fontSize = 12.sp, color = TextPrimary)
                    }
                    Button(
                        onClick = { viewModel.addWorkout("يوم جيم حديد", "GYM", 45, "أوزان ومجموعات") },
                        colors = ButtonDefaults.buttonColors(containerColor = MidnightSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "يوم جيم 🏋️", fontSize = 12.sp, color = TextPrimary)
                    }
                }
            }
        }

        // --- Pillar 4: Habits (العادات) ---
        item {
            val doneCount = habitLogs.size
            val totalCount = habits.size
            val percent = if (totalCount > 0) (doneCount.toFloat() / totalCount * 100).toInt() else 0

            PillarSummaryCard(
                icon = "✅",
                title = "العادات اليومية",
                subtitle = "أنجزت $doneCount من $totalCount عادات اليوم ($percent%)",
                accentColor = BennuGold,
                streak = habitsStreak,
                actionLabel = "إدارة العادات",
                onAction = { viewModel.selectTab(4) }
            ) {
                if (habits.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        habits.take(3).forEach { habit ->
                            val isDone = habitLogs.any { it.habitId == habit.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDone) Color(0x22FFB300) else MidnightSurfaceVariant)
                                    .clickable { viewModel.toggleHabit(habit.id, isDone) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = habit.icon, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = if (isDone) TextGold else TextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) BennuGold else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PillarSummaryCard(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    actionLabel: String,
    streak: Int = 0,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.3f), Color.Transparent))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                    if (streak > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x29FF9100))
                                .border(0.5.dp, Color(0x66FF9100), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "🔥", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$streak أيام",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                }

                TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                    Text(text = actionLabel, color = accentColor, fontSize = 13.sp)
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            content()
        }
    }
}

@Composable
fun PrayerQuickBadge(name: String, done: Boolean, onToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (done) Color(0x3334D399) else MidnightSurfaceVariant)
            .border(1.dp, if (done) PrayerGreen else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (done) PrayerGreen else TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Icon(
            imageVector = if (done) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
            contentDescription = name,
            tint = if (done) PrayerGreen else TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
