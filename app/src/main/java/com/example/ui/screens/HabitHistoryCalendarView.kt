package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.HabitEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitHistoryCalendarView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val allLogs by viewModel.allHabitLogs.collectAsStateWithLifecycle()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { dateFormat.format(Date()) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        dateFormat.format(cal.time)
    }

    var selectedDateString by remember { mutableStateOf(todayStr) }
    var calendarMonthOffset by remember { mutableIntStateOf(0) } // 0 = current month, -1 = last month, etc.

    val currentMonthCalendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("ar")) }
    val monthTitle = remember(calendarMonthOffset) {
        monthYearFormat.format(currentMonthCalendar.time)
    }

    // Map logs by date string: dateString -> Set of habitIds
    val logsByDate = remember(allLogs) {
        allLogs.groupBy { it.dateString }
            .mapValues { entry -> entry.value.map { it.habitId }.toSet() }
    }

    // Completed habits for selected date
    val completedHabitIdsForSelectedDate = logsByDate[selectedDateString] ?: emptySet()
    val completedHabitsForSelectedDate = remember(completedHabitIdsForSelectedDate, habits) {
        habits.filter { it.id in completedHabitIdsForSelectedDate }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Quick-Toggle Shortcuts Header ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(BennuGold.copy(alpha = 0.3f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تقويم سجل العادات 📅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BennuGold
                    )
                    Text(
                        text = "استعرض التزامك اليومي وتاريخ إنجازاتك خطوة بخطوة",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Shortcut buttons for Today and Yesterday
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Today's History Shortcut
                        val isTodaySelected = selectedDateString == todayStr
                        FilterChip(
                            selected = isTodaySelected,
                            onClick = {
                                selectedDateString = todayStr
                                calendarMonthOffset = 0
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "سجل اليوم ☀️", fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BennuGold,
                                selectedLabelColor = Color.Black,
                                containerColor = MidnightSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_today_history")
                        )

                        // Yesterday's History Shortcut
                        val isYesterdaySelected = selectedDateString == yesterdayStr
                        FilterChip(
                            selected = isYesterdaySelected,
                            onClick = {
                                selectedDateString = yesterdayStr
                                val calY = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                                val currentCal = Calendar.getInstance()
                                val diffMonth = (calY.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR)) * 12 +
                                        (calY.get(Calendar.MONTH) - currentCal.get(Calendar.MONTH))
                                calendarMonthOffset = diffMonth
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "سجل أمس ⏪", fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BennuGold,
                                selectedLabelColor = Color.Black,
                                containerColor = MidnightSurfaceVariant,
                                labelColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shortcut_yesterday_history")
                        )
                    }
                }
            }
        }

        // --- Interactive Monthly Calendar ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.radialGradient(listOf(MidnightCardBorder, Color.Transparent))),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_calendar_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { calendarMonthOffset++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MidnightSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر التالي", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = { calendarMonthOffset-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MidnightSurfaceVariant)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر السابق", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of Week Header
                    val weekDays = listOf("سبت", "أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weekDays.forEach { dayName ->
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextMuted,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid
                    val daysInMonth = currentMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val firstDayOfWeek = currentMonthCalendar.get(Calendar.DAY_OF_WEEK) // Sunday=1, Saturday=7

                    // Convert to Saturday-first index (Saturday = 0, Sunday = 1, ... Friday = 6)
                    val saturdayFirstOffset = when (firstDayOfWeek) {
                        Calendar.SATURDAY -> 0
                        Calendar.SUNDAY -> 1
                        Calendar.MONDAY -> 2
                        Calendar.TUESDAY -> 3
                        Calendar.WEDNESDAY -> 4
                        Calendar.THURSDAY -> 5
                        Calendar.FRIDAY -> 6
                        else -> 0
                    }

                    val totalCells = saturdayFirstOffset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayNumber = cellIndex - saturdayFirstOffset + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val dayCal = (currentMonthCalendar.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                        }
                                        val cellDateStr = dateFormat.format(dayCal.time)
                                        val isSelected = cellDateStr == selectedDateString
                                        val isToday = cellDateStr == todayStr

                                        val completedCount = logsByDate[cellDateStr]?.size ?: 0
                                        val totalHabitsCount = habits.size
                                        val hasCompletions = completedCount > 0

                                        // Heatmap intensity background
                                        val heatBackground = when {
                                            isSelected -> BennuGold
                                            completedCount >= 4 -> Color(0xFF15803D) // Vibrant Green
                                            completedCount in 2..3 -> Color(0xFF166534)
                                            completedCount == 1 -> Color(0xFF1E3A2F)
                                            isToday -> Color(0x33FFB300)
                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(heatBackground)
                                                .border(
                                                    width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                                                    color = if (isSelected) Color.White else if (isToday) BennuGold else Color.Transparent,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedDateString = cellDateStr }
                                                .testTag("calendar_day_$cellDateStr"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal),
                                                    color = if (isSelected) Color.Black else if (hasCompletions) Color.White else TextPrimary
                                                )
                                                if (hasCompletions) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.Black else BennuGold)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1E3A2F)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "عادة واحدة", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF15803D)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "عادات مكتملة بكثرة", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }

        // --- Selected Date Summary List ---
        item {
            val selectedDateArabic = remember(selectedDateString) {
                try {
                    val d = dateFormat.parse(selectedDateString)
                    val cal = Calendar.getInstance().apply { time = d ?: Date() }
                    val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SATURDAY -> "السبت"
                        Calendar.SUNDAY -> "الأحد"
                        Calendar.MONDAY -> "الإثنين"
                        Calendar.TUESDAY -> "الثلاثاء"
                        Calendar.WEDNESDAY -> "الأربعاء"
                        Calendar.THURSDAY -> "الخميس"
                        Calendar.FRIDAY -> "الجمعة"
                        else -> ""
                    }
                    val fullDateFmt = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
                    "$dayName، ${fullDateFmt.format(d ?: Date())}"
                } catch (e: Exception) {
                    selectedDateString
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سجل إنجاز: $selectedDateArabic",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = if (selectedDateString == todayStr) "عادات اليوم الحالية" else if (selectedDateString == yesterdayStr) "عادات الأمس" else "عادات هذا اليوم",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGold
                    )
                }

                // Badge count
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (completedHabitsForSelectedDate.isNotEmpty()) Color(0x3310B981) else MidnightSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "${completedHabitsForSelectedDate.size} منجز",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (completedHabitsForSelectedDate.isNotEmpty()) PrayerGreen else TextMuted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (completedHabitsForSelectedDate.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🌱", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد عادات مسجلة كـ 'منجزة' في هذا التاريخ",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "كل يوم هو فرصة جديدة للتجدد والتركيز مع Nour-lv1 ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(completedHabitsForSelectedDate, key = { "hist_${it.id}_$selectedDateString" }) { habit ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(PrayerGreen.copy(alpha = 0.4f), Color.Transparent))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3310B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "تم",
                                    tint = PrayerGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = habit.icon, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "تم الإنجاز واكتساب +10 XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrayerGreen
                                )
                            }
                        }

                        // Streak flame indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22FF9100))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "🔥", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${habit.streakCount} أيام",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                }
            }
        }
    }
}
