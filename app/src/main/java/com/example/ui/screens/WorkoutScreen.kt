package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.example.data.local.entity.WorkoutEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun WorkoutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    val totalMinutes = workouts.sumOf { it.durationMinutes }

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
                            text = "ركن التمرين والجيم 🏋️",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = WorkoutOrange
                        )
                        Text(
                            text = "جسمك القوي يدعم عقلك المذاكر.. +30 XP لكل تمرين",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoutOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("add_workout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "سجل تمرين", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Weekly Summary Card ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    shape = RoundedCornerShape(22.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.radialGradient(listOf(WorkoutOrange.copy(alpha = 0.3f), Color.Transparent))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${workouts.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = WorkoutOrange)
                            Text(text = "تمارين مسجلة", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0x33FFFFFF))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalMinutes د", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BennuGold)
                            Text(text = "إجمالي الوقت", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0x33FFFFFF))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "+${workouts.size * 30}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrayerGreen)
                            Text(text = "XP مكتسب", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            // --- Pre-made Templates Section ---
            item {
                Text(
                    text = "قوالب سريعة للتمارين ⚡",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WorkoutTemplateCard(
                        icon = "🏠",
                        title = "تمرين منزلي سريع (15 دقيقة)",
                        desc = "ضغط، بلانك، سكوات، طعنات — ينشط دورتك الدموية فوراً",
                        duration = "15 دقيقة",
                        onLog = { viewModel.addWorkout("تمرين منزلي سريع", "HOME", 15, "3 مجموعات ضغط وبطن") }
                    )

                    WorkoutTemplateCard(
                        icon = "🏋️",
                        title = "يوم جيم كامل (45 دقيقة)",
                        desc = "أوزان ومجموعات للعضلات الأساسية وبناء قوة بدنية",
                        duration = "45 دقيقة",
                        onLog = { viewModel.addWorkout("يوم جيم كامل", "GYM", 45, "تمارين حديد أساسية") }
                    )

                    WorkoutTemplateCard(
                        icon = "🏃",
                        title = "كارديو وجري (20 دقيقة)",
                        desc = "جري خفيف أو نط حبل لرفع هرمون السعادة وتصفية الذهن",
                        duration = "20 دقيقة",
                        onLog = { viewModel.addWorkout("كارديو وجري", "CARDIO", 20, "جري مستمر") }
                    )
                }
            }

            // --- History Section ---
            if (workouts.isNotEmpty()) {
                item {
                    Text(
                        text = "سجل التمارين السابقة 📋",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                items(workouts, key = { "workout_${it.id}" }) { workout ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (workout.type == "GYM") "🏋️" else if (workout.type == "CARDIO") "🏃" else "🏠",
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = workout.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${workout.durationMinutes} دقيقة • +${workout.xpEarned} XP",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextGold
                                    )
                                }
                            }

                            IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "حذف", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Custom Workout Dialog ---
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var durationText by remember { mutableStateOf("20") }
        var type by remember { mutableStateOf("HOME") }
        var setsReps by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = MidnightSurface,
            title = {
                Text(text = "تسجيل تمرين جديد 💪", fontWeight = FontWeight.Bold, color = WorkoutOrange)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("اسم التمرين (مثلاً: تمرين ظهر وباي)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoutOrange,
                            unfocusedBorderColor = MidnightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                        label = { Text("المدة بالدقائق", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoutOrange,
                            unfocusedBorderColor = MidnightCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("HOME", "منزلي 🏠", WorkoutOrange),
                            Triple("GYM", "جيم 🏋️", BennuGold),
                            Triple("CARDIO", "كارديو 🏃", StudyCyan)
                        ).forEach { (tKey, tLabel, tColor) ->
                            val isSel = type == tKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) tColor.copy(alpha = 0.25f) else MidnightSurfaceVariant)
                                    .border(1.dp, if (isSel) tColor else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { type = tKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = tLabel, fontSize = 12.sp, color = if (isSel) tColor else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val dur = durationText.toIntOrNull() ?: 20
                            viewModel.addWorkout(title, type, dur, setsReps)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoutOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "حفظ وإضافة XP", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = "إلغاء", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun WorkoutTemplateCard(
    icon: String,
    title: String,
    desc: String,
    duration: String,
    onLog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(WorkoutOrange.copy(alpha = 0.35f), Color.Transparent))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Button(
                onClick = onLog,
                colors = ButtonDefaults.buttonColors(containerColor = WorkoutOrange.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = "سجل الآن", color = WorkoutOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
