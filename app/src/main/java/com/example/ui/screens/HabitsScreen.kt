package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.local.entity.HabitEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val habitLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Daily Checklist, 1: History Calendar, 2: Analytics

    val completedCount = habitLogs.size
    val totalCount = habits.size
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(modifier = modifier.fillMaxSize()) {
        // --- Top Navigation Tabs (Daily, Calendar, Analytics) ---
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MidnightSurface,
            contentColor = BennuGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = BennuGold,
                    height = 3.dp
                )
            },
            divider = {
                HorizontalDivider(color = MidnightCardBorderSubtle)
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "قائمتي اليومية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                selectedContentColor = BennuGold,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_habits_daily")
            )

            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تقويم السجل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                selectedContentColor = BennuGold,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_habits_calendar")
            )

            Tab(
                selected = selectedSubTab == 2,
                onClick = {
                    selectedSubTab = 2
                    viewModel.refreshHabitAnalytics()
                },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "التحليلات والتقدم", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                selectedContentColor = BennuGold,
                unselectedContentColor = TextMuted,
                modifier = Modifier.testTag("tab_habits_analytics")
            )
        }

        // --- Sub-Screen Content ---
        when (selectedSubTab) {
            0 -> {
                // Daily Habits Checklist
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
                                    text = "ركن العادات والانضباط ✅",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BennuGold
                                )
                                Text(
                                    text = "العادات الصغيرة المستمرة تصنع التميز.. +10 XP لكل عادة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Button(
                                onClick = { showAddHabitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BennuGold),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("add_habit_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عادة", tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "عادة جديدة", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- Daily Habits Progress Card ---
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                            shape = RoundedCornerShape(22.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.radialGradient(listOf(BennuGold.copy(alpha = 0.35f), Color.Transparent))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إنجاز عادات اليوم 🌱",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "أنجزت $completedCount من أصل $totalCount عادات",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (completedCount == totalCount && totalCount > 0) "يومك مثالي وNour-lv1 فرحان بيك جداً! 🔥" else "كمّل باقي العادات عشان ترفع لفل Nour-lv1!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { progressFraction.coerceIn(0f, 1f) },
                                        strokeWidth = 7.dp,
                                        color = BennuGold,
                                        trackColor = MidnightSurfaceVariant,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // --- Habits Checklist Header ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "قائمتك اليومية 📋",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "اضغط مطولاً أو استخدم 🗑️ لحذف عادة",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    items(habits, key = { "habit_${it.id}" }) { habit ->
                        val isDone = habitLogs.any { it.habitId == habit.id }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDone) Color(0xFF1B243B) else MidnightSurface
                            ),
                            shape = RoundedCornerShape(18.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        if (isDone) BennuGold.copy(alpha = 0.5f) else MidnightCardBorderSubtle,
                                        Color.Transparent
                                    )
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { viewModel.toggleHabit(habit.id, isDone) },
                                    onLongClick = { habitToDelete = habit }
                                )
                                .testTag("habit_card_${habit.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) BennuGold else MidnightSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "تم", tint = Color.Black, modifier = Modifier.size(20.dp))
                                        } else {
                                            Icon(imageVector = Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = habit.icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = habit.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isDone) TextGold else TextPrimary
                                            )
                                        }
                                        Text(
                                            text = if (isDone) "تمت اليوم بنجاح (+10 XP) ✨" else "اضغط للتسجيل اليومي",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDone) PrayerGreen else TextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Streak flame indicator
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x22FF9100))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "🔥", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${habit.streakCount + (if (isDone) 1 else 0)}d",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFFFB74D)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Delete Habit Button
                                    IconButton(
                                        onClick = { habitToDelete = habit },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_habit_btn_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "حذف العادة",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- Suggested Habit Starters ---
                    item {
                        Text(
                            text = "عادات مقترحة للإضافة السريعة 💡",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
                        val suggestions = listOf(
                            Pair("أذكار الصباح والمساء", "📿"),
                            Pair("مشي 15 دقيقة في الهواء", "🚶"),
                            Pair("كتابة يوميات وتفريغ ذهني", "✍️"),
                            Pair("شرب 2 لتر ماء يومياً", "💧")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggestions.forEach { (title, icon) ->
                                val alreadyAdded = habits.any { it.title == title }
                                if (!alreadyAdded) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MidnightSurfaceVariant)
                                            .clickable { viewModel.addHabit(title, icon) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                        }
                                        Text(text = "+ أضف", color = BennuGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // History Calendar Tab
                HabitHistoryCalendarView(viewModel = viewModel)
            }
            2 -> {
                // Habit Analytics & Progress Screen
                HabitAnalyticsScreen(viewModel = viewModel)
            }
        }
    }

    // --- Mandatory Delete Confirmation Dialog ---
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor = MidnightSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0x33EF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = UrgentRose,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "حذف العادة نهائياً؟",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف عادة «${habit.icon} ${habit.title}» وسجل إنجازاتها التاريخي بالكامل؟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ لا يمكن التراجع عن هذه العملية بعد التأكيد.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = UrgentRose
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteHabit(habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRose, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_delete_habit_button")
                ) {
                    Text(text = "نعم، حذف نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { habitToDelete = null },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_delete_habit_button")
                ) {
                    Text(text = "إلغاء")
                }
            }
        )
    }

    // --- Add Habit Dialog ---
    if (showAddHabitDialog) {
        var title by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("✨") }

        val icons = listOf("✨", "💧", "📖", "🌙", "🪥", "🏃", "📿", "🍎", "🧘", "✍️")

        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            containerColor = MidnightSurface,
            title = {
                Text(text = "إضافة عادة يومية جديدة 🌱", fontWeight = FontWeight.Bold, color = BennuGold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("اسم العادة (مثلاً: الاستيقاظ 6:30 ص)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BennuGold,
                            unfocusedBorderColor = MidnightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_habit_title_input")
                    )

                    Text(text = "اختر أيقونة العادة:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        icons.take(5).forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedIcon == icon) BennuGold else MidnightSurfaceVariant)
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        icons.drop(5).forEach { icon ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedIcon == icon) BennuGold else MidnightSurfaceVariant)
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addHabit(title.trim(), selectedIcon)
                            showAddHabitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BennuGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_add_habit_button")
                ) {
                    Text(text = "حفظ العادة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text(text = "إلغاء", color = TextMuted)
                }
            }
        )
    }
}
