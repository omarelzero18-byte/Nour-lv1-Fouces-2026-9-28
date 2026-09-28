package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bennu.BennuCharacter
import com.example.bennu.BennuStage
import com.example.ui.WeeklyStats
import com.example.ui.theme.*

@Composable
fun WeeklyWrappedDialog(
    stats: WeeklyStats,
    userName: String,
    stage: BennuStage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(28.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFEC4899),
                        Color(0xFF8B5CF6),
                        BennuGold
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ملخصك الأسبوعي مع Nour-lv1 ✨",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF472B6)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Celebratory Bennu
                BennuCharacter(
                    stage = stage,
                    size = 110.dp,
                    isCelebrating = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "«Fucos» — حصاد الأسبوع",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BennuGold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "عاش يا $userName! شوفت عملت إيه الأسبوع ده؟ 🔥",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wrapped Stats Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WrappedStatRow(label = "أكتر يوم كنت فيه متألق:", value = "يوم ${stats.bestDayArabic} 🌟", valueColor = BennuGold)
                    WrappedStatRow(label = "إجمالي الـ XP المكتسب:", value = "+${stats.totalXp} XP ⚡", valueColor = Color(0xFF38BDF8))
                    WrappedStatRow(label = "مهام دراسية أنجزتها:", value = "${stats.tasksCompleted} مهام 📚", valueColor = PrayerGreen)
                    WrappedStatRow(label = "تمارين رياضية مسجلة:", value = "${stats.workoutsDone} حصص 🏋️", valueColor = WorkoutOrange)
                    WrappedStatRow(label = "نسبة التزام الصلاة:", value = "${stats.prayerRatePercent}% 🕌", valueColor = Color(0xFFA78BFA))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Warm Egyptian Bennu Celebration Speech
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1B4B))
                        .border(1.dp, Color(0x66A855F7), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = stats.bennuCelebrationMsg,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = Color(0xFFF3E8FF),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share Button
                Button(
                    onClick = {
                        val shareText = """
🚀 حصاد أسبوعي في تطبيق «Fucos»:
✨ حققت ${stats.totalXp} XP وNour-lv1 فخور بيا!
📚 أنجزت ${stats.tasksCompleted} مهام دراسية
🕌 نسبة التزام الصلاة: ${stats.prayerRatePercent}%
🏋️ ${stats.workoutsDone} تمارين ونشاط رياضي
«ركز وارتقِ كل يوم خطوة» 🦅🔥
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "شارك إنجازك الأسبوعي"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BennuGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "شارك ملخصك الأسبوعي 🎉", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WrappedStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}
