package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.PrayerTime
import com.example.util.PrayerTimesCalculator

@Composable
fun PrayerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val prayers by viewModel.todayPrayers.collectAsStateWithLifecycle()
    val totalPrayerStreak by viewModel.prayerStreak.collectAsStateWithLifecycle()
    val individualStreaks by viewModel.individualPrayerStreaks.collectAsStateWithLifecycle()
    val dailyWisdom = remember { PrayerTimesCalculator.getDailyWisdom() }

    val nextPrayer = prayerTimes.nextPrayer
    val minutesLeft = prayerTimes.minutesUntilNext

    LazyColumn(
        modifier = modifier
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
                        text = "ركن الصلاة 🕌",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrayerGreen
                    )
                    Text(
                        text = "صلاتك نور يومك وراحة بالك.. واكسب +15 XP مع كل فرض",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (totalPrayerStreak > 0) {
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
                            text = "$totalPrayerStreak أيام",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }
        }

        // --- Next Prayer Hero Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(24.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.radialGradient(listOf(PrayerGreen.copy(alpha = 0.4f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "مواقيت فلكية دقيقة بدون إنترنت", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3334D399))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "اليوم", color = PrayerGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "الصلاة القادمة: ${nextPrayer?.nameArabic ?: "الفجر"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = nextPrayer?.formattedTime ?: "",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrayerGreen
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "متبقي حوالي $minutesLeft دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGold
                    )
                }
            }
        }

        // --- Daily Wisdom Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2329)),
                shape = RoundedCornerShape(18.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PrayerGreen.copy(alpha = 0.25f), Color.Transparent))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🌿", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "نفحة اليوم الروحية",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrayerGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dailyWisdom,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // --- 5 Daily Prayers List ---
        item {
            Text(
                text = "الصلوات الخمس المفروضة 🕌",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrayerItemCard(
                    name = "الفجر",
                    time = prayerTimes.fajr.formattedTime,
                    isDone = prayers?.fajrDone == true,
                    streak = individualStreaks["fajr"] ?: 0,
                    onToggle = { viewModel.togglePrayer("fajr") }
                )

                PrayerItemCard(
                    name = "الشروق (سنة)",
                    time = prayerTimes.sunrise.formattedTime,
                    isDone = false,
                    isOptional = true,
                    onToggle = {}
                )

                PrayerItemCard(
                    name = "الظهر",
                    time = prayerTimes.dhuhr.formattedTime,
                    isDone = prayers?.dhuhrDone == true,
                    streak = individualStreaks["dhuhr"] ?: 0,
                    onToggle = { viewModel.togglePrayer("dhuhr") }
                )

                PrayerItemCard(
                    name = "العصر",
                    time = prayerTimes.asr.formattedTime,
                    isDone = prayers?.asrDone == true,
                    streak = individualStreaks["asr"] ?: 0,
                    onToggle = { viewModel.togglePrayer("asr") }
                )

                PrayerItemCard(
                    name = "المغرب",
                    time = prayerTimes.maghrib.formattedTime,
                    isDone = prayers?.maghribDone == true,
                    streak = individualStreaks["maghrib"] ?: 0,
                    onToggle = { viewModel.togglePrayer("maghrib") }
                )

                PrayerItemCard(
                    name = "العشاء",
                    time = prayerTimes.isha.formattedTime,
                    isDone = prayers?.ishaDone == true,
                    streak = individualStreaks["isha"] ?: 0,
                    onToggle = { viewModel.togglePrayer("isha") }
                )
            }
        }
    }
}

@Composable
fun PrayerItemCard(
    name: String,
    time: String,
    isDone: Boolean,
    streak: Int = 0,
    isOptional: Boolean = false,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFF102824) else MidnightSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isDone) PrayerGreen.copy(alpha = 0.5f) else MidnightCardBorderSubtle,
                    Color.Transparent
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOptional, onClick = onToggle)
            .testTag("prayer_card_$name")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDone) PrayerGreen else Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "أنجزت", tint = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(imageVector = Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDone) PrayerGreen else TextPrimary
                        )
                        if (streak > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x22FF9100))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(text = "🔥", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$streak أيام",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFB74D),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isOptional) "وقت الشروق" else if (isDone) "تمت الصلاة بفضل الله (+15 XP) ✨" else "اضغط للتسجيل بعد الصلاة",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDone) TextGold else TextSecondary
                    )
                }
            }

            Text(
                text = time,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDone) PrayerGreen else TextPrimary
            )
        }
    }
}
