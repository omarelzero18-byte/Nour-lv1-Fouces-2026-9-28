package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun HabitAnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.habitAnalytics.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Screen Header ---
        item {
            Column {
                Text(
                    text = "تحليلات العادات والتقدم 📊",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BennuGold
                )
                Text(
                    text = "رؤى دقيقة حول معدلات التزامك، عاداتك الراسخة، والعادات التي تحتاج تدخلاً",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // --- 1. Overall Progress Score Card (Circular Progress Bar - Last 7 Days) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(22.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.radialGradient(
                        listOf(BennuGold.copy(alpha = 0.45f), Color.Transparent)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_progress_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = BennuGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "معدل التقدم الإجمالي",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "آخر 7 أيام: تم إنجاز ${analytics.totalCompletions7Days} عادة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val evaluationText = when {
                            analytics.overallProgressPercent >= 80 -> "أداء استثنائي وانضباط حديدي! 🌟"
                            analytics.overallProgressPercent >= 50 -> "تقدم جيد ومستمر.. نحو القمة! 🚀"
                            analytics.overallProgressPercent > 0 -> "بداية تدريجية.. خطوة بخطوة تكبر العادات 🌱"
                            else -> "ابدأ بإنجاز أول عادة اليوم لرفع مؤشرك! ✨"
                        }
                        Text(
                            text = evaluationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Circular Progress Bar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(88.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { (analytics.overallProgressPercent / 100f).coerceIn(0f, 1f) },
                            strokeWidth = 9.dp,
                            color = BennuGold,
                            trackColor = MidnightSurfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${analytics.overallProgressPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "إنجاز 7 أيام",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Neglect Alert (Nour-lv1 Proactive Motivational Prompt) ---
        analytics.neglectAlert?.let { alert ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261918)),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(UrgentRose.copy(alpha = 0.6f), Color(0xFFFF9100).copy(alpha = 0.4f))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("neglect_alert_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, UrgentRose, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.nour_avatar),
                                    contentDescription = "Nour-lv1",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "تنبيه إهمال من Nour-lv1 🚨",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = UrgentRose
                                    )
                                }
                                Text(
                                    text = "تذكير تشجيعي لطيف لعادة '${alert.habitIcon} ${alert.habitTitle}'",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Speech Bubble
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1B141E))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = alert.motivationalPrompt,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Most Neglected Habits (Primary Requirement - Top 3-5 Lowest Completion) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(UrgentRose.copy(alpha = 0.4f), Color.Transparent)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("most_neglected_habits_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33EF4444)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = UrgentRose,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "العادات الأكثر إهمالاً ⚠️",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = UrgentRose
                                )
                                Text(
                                    text = "أطول فترة انقطاع وأقل تردد في آخر 14 إلى 30 يوماً",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (analytics.mostNeglectedHabits.isEmpty()) {
                        Text(
                            text = "لا توجد عادات مهملة حالياً.. كل عاداتك نشطة وملتزم بها! 🎉",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrayerGreen,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            analytics.mostNeglectedHabits.forEach { item ->
                                val isDoneToday = todayLogs.any { it.habitId == item.habit.id }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MidnightSurfaceVariant)
                                        .border(1.dp, Color(0x33EF4444), RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = item.habit.icon, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = item.habit.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "متوقفة منذ ${item.daysSinceLastCompleted} أيام",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFFCA5A5)
                                                )
                                                Text(text = " • ", color = TextMuted)
                                                Text(
                                                    text = "${item.completionsLast30Days}/30 يوماً",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }

                                    // Quick Action to complete or indicate done
                                    Button(
                                        onClick = { viewModel.toggleHabit(item.habit.id, isDoneToday) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDoneToday) PrayerGreen else Color(0x33EF4444),
                                            contentColor = if (isDoneToday) Color.Black else UrgentRose
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("neglected_habit_btn_${item.habit.id}")
                                    ) {
                                        if (isDoneToday) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "تم", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "تمت اليوم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(text = "إنجاز الآن 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Top Consistently Formed Habits (Top 3-5 with Highest Streaks) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(PrayerGreen.copy(alpha = 0.4f), Color.Transparent)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_consistent_habits_section")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x3310B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏆", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "العادات الأكثر التزاماً وتكويناً 🔥",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrayerGreen
                                )
                                Text(
                                    text = "عادات صنعت بها أطول سلسلة استمرار وأعلى ثبات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (analytics.topConsistentHabits.isEmpty()) {
                        Text(
                            text = "سجل إنجازاتك اليومية لتتصدر العادات قائمة الأكثر التزاماً ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            analytics.topConsistentHabits.forEachIndexed { index, perf ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MidnightSurfaceVariant)
                                        .border(1.dp, MidnightCardBorderSubtle, RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = perf.habit.icon, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = perf.habit.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${perf.completionsLast7Days}/7 أيام هذا الأسبوع",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextGold
                                                )
                                                Text(text = " • ", color = TextMuted)
                                                Text(
                                                    text = "${perf.completionRatePercent}% التزام",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = PrayerGreen
                                                )
                                            }
                                        }
                                    }

                                    // Streak badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x22FF9100))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "🔥", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${perf.streak} أيام",
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
        }
    }
}
