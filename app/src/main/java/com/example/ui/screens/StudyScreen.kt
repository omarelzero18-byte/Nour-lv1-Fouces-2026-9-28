package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StudyCalendarDay(
    val dateString: String,
    val dayNameArabic: String,
    val dayNumber: Int,
    val monthNameArabic: String,
    val startOfDayMillis: Long,
    val endOfDayMillis: Long,
    val isToday: Boolean
)

fun parseHexColor(hex: String, fallback: Color = StudyCyan): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0xFF000000)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val exams by viewModel.upcomingExams.collectAsStateWithLifecycle()
    val pomodoroState by viewModel.pomodoroState.collectAsStateWithLifecycle()
    val subjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val subjectMap = remember(subjects) { subjects.associateBy { it.id } }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var dialogDueDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var viewMode by remember { mutableStateOf("LIST") } // "LIST" or "CALENDAR"

    // Generate Calendar Days (14 days: today - 1 to today + 12)
    val calendarDays = remember {
        val list = mutableListOf<StudyCalendarDay>()
        val cal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1) // start 1 day back

        val dfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dfDayName = SimpleDateFormat("EEE", Locale("ar"))
        val dfMonth = SimpleDateFormat("MMMM", Locale("ar"))

        for (i in 0 until 14) {
            val key = dfKey.format(cal.time)
            val dayName = dfDayName.format(cal.time)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            val monthName = dfMonth.format(cal.time)

            val startCal = cal.clone() as Calendar
            startCal.set(Calendar.HOUR_OF_DAY, 0)
            startCal.set(Calendar.MINUTE, 0)
            startCal.set(Calendar.SECOND, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            val startMillis = startCal.timeInMillis

            val endCal = cal.clone() as Calendar
            endCal.set(Calendar.HOUR_OF_DAY, 23)
            endCal.set(Calendar.MINUTE, 59)
            endCal.set(Calendar.SECOND, 59)
            endCal.set(Calendar.MILLISECOND, 999)
            val endMillis = endCal.timeInMillis

            list.add(
                StudyCalendarDay(
                    dateString = key,
                    dayNameArabic = dayName,
                    dayNumber = dayNum,
                    monthNameArabic = monthName,
                    startOfDayMillis = startMillis,
                    endOfDayMillis = endMillis,
                    isToday = key == todayStr
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val todayDateString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var selectedCalendarDateString by remember { mutableStateOf(todayDateString) }
    val selectedDay = calendarDays.find { it.dateString == selectedCalendarDateString } ?: calendarDays.firstOrNull()

    val urgentTasks = tasks.filter { !it.isCompleted && it.priority == "URGENT" }
    val normalTasks = tasks.filter { !it.isCompleted && it.priority == "NORMAL" }
    val laterTasks = tasks.filter { !it.isCompleted && it.priority == "LATER" }
    val completedTasks = tasks.filter { it.isCompleted }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ركن المذاكرة 📚",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudyCyan
                        )
                        Text(
                            text = "رتّب أولوياتك واكسب XP مع كل مهمة بتخلصها",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            dialogDueDateMillis = System.currentTimeMillis() + 86400000L
                            showAddTaskDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudyCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_task_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "إضافة مهمة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Toggle: List View vs Calendar View ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MidnightSurface)
                        .border(1.dp, MidnightCardBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isList = viewMode == "LIST"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isList) StudyCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewMode = "LIST" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = null,
                                tint = if (isList) StudyCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "قائمة المهام",
                                color = if (isList) StudyCyan else TextSecondary,
                                fontWeight = if (isList) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }

                    val isCal = viewMode == "CALENDAR"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCal) StudyCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewMode = "CALENDAR" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isCal) StudyCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تقويم المذاكرة",
                                color = if (isCal) StudyCyan else TextSecondary,
                                fontWeight = if (isCal) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (viewMode == "CALENDAR") {
                // --- CALENDAR VIEW ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StudyCyan.copy(alpha = 0.3f), Color.Transparent))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "📅", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تقويم الأيام القادمة",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StudyCyan
                                    )
                                }
                                Text(
                                    text = selectedDay?.monthNameArabic ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal Days Scroll Strip
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(calendarDays) { day ->
                                    val isSelected = day.dateString == selectedCalendarDateString
                                    // Count tasks / exams on this day
                                    val dayTasks = tasks.filter {
                                        val time = if (it.isExam && it.examDateMillis > 0) it.examDateMillis else it.dueDateMillis
                                        time in day.startOfDayMillis..day.endOfDayMillis
                                    }
                                    val hasExam = dayTasks.any { it.isExam }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(54.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) StudyCyan.copy(alpha = 0.25f)
                                                else if (day.isToday) Color(0x33FFB300)
                                                else MidnightSurfaceVariant
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) StudyCyan else if (day.isToday) BennuGold else Color.Transparent,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedCalendarDateString = day.dateString }
                                            .padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = day.dayNameArabic,
                                            fontSize = 11.sp,
                                            color = if (isSelected) StudyCyan else TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${day.dayNumber}",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) StudyCyan else if (day.isToday) BennuGold else TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Indicator dot
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.height(6.dp)
                                        ) {
                                            if (hasExam) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(UrgentRose)
                                                )
                                            } else if (dayTasks.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(StudyCyan)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tasks for Selected Calendar Day
                item {
                    val dayTasks = if (selectedDay != null) {
                        tasks.filter {
                            val time = if (it.isExam && it.examDateMillis > 0) it.examDateMillis else it.dueDateMillis
                            time in selectedDay.startOfDayMillis..selectedDay.endOfDayMillis
                        }
                    } else emptyList()

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مهام يوم ${selectedDay?.dayNameArabic ?: ""} (${selectedDay?.dateString ?: ""})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )

                            TextButton(
                                onClick = {
                                    dialogDueDateMillis = selectedDay?.startOfDayMillis ?: (System.currentTimeMillis() + 86400000L)
                                    showAddTaskDialog = true
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = StudyCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "مهمة لهذا اليوم", color = StudyCyan, fontSize = 12.sp)
                            }
                        }

                        if (dayTasks.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                shape = RoundedCornerShape(16.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MidnightCardBorderSubtle, Color.Transparent))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🌿", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "مفيش مهام مسجلة لليوم ده!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "يوم رايق ومثالي للمراجعة أو تصفية الذهن.. أو اضغط فوق لإضافة مهمة.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            dayTasks.forEach { task ->
                                TaskCard(
                                    task = task,
                                    subject = subjectMap[task.subjectId],
                                    onToggle = { viewModel.toggleTask(task) },
                                    onDelete = { viewModel.deleteTask(task.id) }
                                )
                            }
                        }
                    }
                }
            } else {
                // --- LIST VIEW ---

                // Emergency Mode Exam Countdown
                if (exams.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF28111A)),
                            shape = RoundedCornerShape(22.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(UrgentRose, Color(0xFFFF8A80)))),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🚨", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "وضع الطوارئ — جدول الامتحانات",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = UrgentRose
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(UrgentRose.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${exams.size} امتحانات قادمة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UrgentRose
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                exams.forEach { exam ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MidnightSurface)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = exam.isCompleted,
                                                onCheckedChange = { viewModel.toggleTask(exam) },
                                                colors = CheckboxDefaults.colors(checkedColor = UrgentRose)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    val sub = subjectMap[exam.subjectId]
                                                    if (sub != null) {
                                                        val sColor = parseHexColor(sub.colorHex)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(sColor.copy(alpha = 0.2f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(text = sub.name, color = sColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }
                                                    Text(
                                                        text = exam.title,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = TextPrimary
                                                    )
                                                }
                                                Text(
                                                    text = "Nour-lv1 بيشجعك: ركز في البومودورو وهتعدي بإذن الله! 🔥",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextGold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                // Integrated Pomodoro Timer Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                        shape = RoundedCornerShape(22.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BennuGold.copy(alpha = 0.3f), Color.Transparent))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⏱️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (pomodoroState.isBreak) "وقت الاستراحة ☕" else "مؤقت بومودورو 🧠",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (pomodoroState.isBreak) PrayerGreen else BennuGold
                                    )
                                }
                                Text(
                                    text = "+25 XP عند الإكمال",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextGold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Timer Display
                            val minutes = pomodoroState.remainingSeconds / 60
                            val seconds = pomodoroState.remainingSeconds % 60
                            val timerString = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                            val progress = pomodoroState.remainingSeconds.toFloat() / pomodoroState.totalSeconds.coerceAtLeast(1)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(160.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 10.dp,
                                    color = if (pomodoroState.isBreak) PrayerGreen else BennuGold,
                                    trackColor = MidnightSurfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = timerString,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (pomodoroState.isRunning) "مركّز مع Nour-lv1 ✨" else "جاهز؟",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.resetPomodoro() },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MidnightSurfaceVariant)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = TextSecondary)
                                }

                                Button(
                                    onClick = { viewModel.togglePomodoro() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pomodoroState.isRunning) UrgentRose else BennuGold,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("pomodoro_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (pomodoroState.isRunning) "إيقاف مؤقت" else "ابدأ"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (pomodoroState.isRunning) "إيقاف مؤقت" else "ابدأ الجلسة",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Urgent Tasks
                if (urgentTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "🚨 مهام عاجلة ومهمة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = UrgentRose
                        )
                    }
                    items(urgentTasks, key = { "urgent_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            subject = subjectMap[task.subjectId],
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }

                // Section: Normal Priority Tasks
                if (normalTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "📘 مهام وواجبات دراسية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudyCyan
                        )
                    }
                    items(normalTasks, key = { "normal_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            subject = subjectMap[task.subjectId],
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }

                // Section: Later Priority Tasks
                if (laterTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "⏳ مهام مؤجلة أو لاحقة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                    }
                    items(laterTasks, key = { "later_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            subject = subjectMap[task.subjectId],
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }

                // Section: Completed Tasks
                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            text = "✅ مهام أنجزتها بنجاح (${completedTasks.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrayerGreen
                        )
                    }
                    items(completedTasks, key = { "done_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            subject = subjectMap[task.subjectId],
                            onToggle = { viewModel.toggleTask(task) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Add Task / Exam Dialog ---
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var isExam by remember { mutableStateOf(false) }
        var priority by remember { mutableStateOf("NORMAL") } // URGENT, NORMAL, LATER
        var selectedSubjectId by remember { mutableStateOf(0L) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = MidnightSurface,
            title = {
                Text(
                    text = if (isExam) "إضافة امتحان جديد 📝" else "إضافة مهمة دراسية 📚",
                    fontWeight = FontWeight.Bold,
                    color = if (isExam) UrgentRose else StudyCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        placeholder = { Text("مثلاً: حل شيت الفيزياء أو امتحان كيمياء", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudyCyan,
                            unfocusedBorderColor = MidnightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Subject Selector Chips
                    Text(text = "المادة الدراسية:", color = TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isNoSub = selectedSubjectId == 0L
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isNoSub) StudyCyan.copy(alpha = 0.25f) else MidnightSurfaceVariant)
                                .border(1.dp, if (isNoSub) StudyCyan else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedSubjectId = 0L }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "عام / بدون مادة",
                                fontSize = 12.sp,
                                color = if (isNoSub) StudyCyan else TextSecondary,
                                fontWeight = if (isNoSub) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        subjects.forEach { sub ->
                            val isSel = selectedSubjectId == sub.id
                            val sColor = parseHexColor(sub.colorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) sColor.copy(alpha = 0.25f) else MidnightSurfaceVariant)
                                    .border(1.dp, if (isSel) sColor else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedSubjectId = sub.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sub.name,
                                    fontSize = 12.sp,
                                    color = if (isSel) sColor else TextSecondary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Is Exam Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "هذا موعد امتحان (وضع الطوارئ)", color = TextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = isExam,
                            onCheckedChange = { isExam = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = UrgentRose, checkedTrackColor = UrgentRose.copy(alpha = 0.4f))
                        )
                    }

                    // Priority Selector
                    Text(text = "درجة الأولوية:", color = TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("URGENT", "عاجل 🚨", UrgentRose),
                            Triple("NORMAL", "عادي 📘", StudyCyan),
                            Triple("LATER", "مؤجل ⏳", TextMuted)
                        ).forEach { (pKey, pLabel, pColor) ->
                            val isSel = priority == pKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) pColor.copy(alpha = 0.25f) else MidnightSurfaceVariant)
                                    .border(1.dp, if (isSel) pColor else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { priority = pKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = pLabel, fontSize = 12.sp, color = if (isSel) pColor else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(
                                title = taskTitle,
                                subjectId = selectedSubjectId,
                                priority = priority,
                                isExam = isExam,
                                dueDateMillis = dialogDueDateMillis
                            )
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isExam) UrgentRose else StudyCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "حفظ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(text = "إلغاء", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    subject: SubjectEntity? = null,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(
            if (task.priority == "URGENT") UrgentRose.copy(alpha = 0.4f) else MidnightCardBorderSubtle,
            Color.Transparent
        ))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (task.priority == "URGENT") UrgentRose else StudyCyan,
                        uncheckedColor = TextMuted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (subject != null) {
                            val subjectColor = parseHexColor(subject.colorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(subjectColor.copy(alpha = 0.2f))
                                    .border(0.5.dp, subjectColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = subject.name,
                                    color = subjectColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task.isCompleted) TextMuted else TextPrimary
                        )
                    }
                    if (task.isExam) {
                        Text(
                            text = "امتحان — وضع الطوارئ 🚨",
                            style = MaterialTheme.typography.labelSmall,
                            color = UrgentRose
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "حذف", tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}
