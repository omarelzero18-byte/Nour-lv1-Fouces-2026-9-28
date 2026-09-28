package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bennu.BennuCharacter
import com.example.bennu.BennuStage
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices

@Composable
fun OnboardingScreen(
    onComplete: (name: String, starterHabits: List<Pair<String, String>>, lat: Double?, lon: Double?) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) } // 1: Egg hatch, 2: Name, 3: Habits, 4: Location, 5: First win!
    var userName by remember { mutableStateOf("") }
    val selectedHabits = remember { mutableStateListOf<Pair<String, String>>() }

    var locationDetermined by remember { mutableStateOf(false) }
    var userLatitude by remember { mutableStateOf<Double?>(null) }
    var userLongitude by remember { mutableStateOf<Double?>(null) }
    var locationRequested by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationRequested = true
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLatitude = loc.latitude
                        userLongitude = loc.longitude
                        locationDetermined = true
                    }
                }
            } catch (_: SecurityException) {
                // Silently fallback to Cairo defaults
            }
        }
    }

    val suggestedHabits = listOf(
        Pair("شرب 2 لتر مية يومياً", "💧"),
        Pair("قراءة 10 دقايق في كتاب", "📖"),
        Pair("تنظيف الأسنان قبل النوم", "🪥"),
        Pair("النوم بدري قبل 11:30", "🌙")
    )

    // Initially select 2 habits
    LaunchedEffect(Unit) {
        if (selectedHabits.isEmpty()) {
            selectedHabits.add(suggestedHabits[0])
            selectedHabits.add(suggestedHabits[1])
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightBackground, Color(0xFF0F172A), MidnightBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Step indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (i == step) 28.dp else 12.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) BennuGold else Color(0x33FFFFFF)
                            )
                    )
                }
            }

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it / 2 } togetherWith fadeOut() + slideOutHorizontally { -it / 2 }
                },
                label = "onboarding_steps",
                modifier = Modifier.weight(1f)
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        1 -> {
                            // Step 1: Welcome & Bennu Egg
                            BennuCharacter(
                                stage = BennuStage.EGG,
                                size = 180.dp,
                                modifier = Modifier.testTag("onboarding_egg")
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "مرحباً بك في «Fucos»",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = BennuGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "«ركز وارتقِ كل يوم خطوة»",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "حياتك اليومية متوازنة في 4 ركائز: عقلك (المذاكرة)، روحك (الصلاة)، جسدك (التمرين)، وانضباطك (العادات). ومعاك «Nour-lv1» رفيقك الذكي اللي هيكبر معاك!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        2 -> {
                            // Step 2: Name Input
                            BennuCharacter(
                                stage = BennuStage.CHICK,
                                size = 150.dp,
                                isCelebrating = true
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "إيه اسمك يا بطل؟",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nour-lv1 مستني يناديك بيه ويبدأ رحلتك سوا!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                placeholder = { Text("اكتب اسمك هنا (مثلاً: عمر)", color = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BennuGold,
                                    unfocusedBorderColor = MidnightCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = MidnightSurfaceVariant,
                                    unfocusedContainerColor = MidnightSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .testTag("name_input")
                            )
                        }

                        3 -> {
                            // Step 3: Starter Habits
                            Text(
                                text = "اختر عاداتك الأولى 🌱",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = BennuGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "انضباطك اليومي بيغذي بريق Nour-lv1 ويكبره كل يوم",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                suggestedHabits.forEach { habit ->
                                    val isSelected = selectedHabits.contains(habit)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0x33FFB300) else MidnightSurface)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (isSelected) BennuGold else MidnightCardBorderSubtle,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                if (isSelected) selectedHabits.remove(habit)
                                                else selectedHabits.add(habit)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = habit.second, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = habit.first,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isSelected) BennuGoldLight else TextPrimary
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) BennuGold else Color(0x22FFFFFF)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "محدد",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        4 -> {
                            // Step 4: Prayer Location Rationale & GPS Request
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "الموقع",
                                tint = BennuGold,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "مواقيت الصلاة الدقيقة 🕌",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nour-lv1 بيحسب مواقيت الصلاة الخمس بدقة فلكية حسب موقعك الجغرافي الفعلي مجاناً وبدون إنترنت، عشان ينبهك بلطف وتثبت على صلاتك.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (locationDetermined) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10281E)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PrayerGreen, Color.Transparent))),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "✅", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "تم تحديد موقعك بدقة!",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PrayerGreen
                                            )
                                            Text(
                                                text = "سيتم حساب أوقات الأذان والعد التنازلي لمدينتك بدقة فلكية.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        locationLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrayerGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "تحديد موقعي للصلاة 📍", fontWeight = FontWeight.Bold)
                                }

                                if (locationRequested && !locationDetermined) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "سيتم استخدام توقيت القاهرة كخيار افتراضي سلس.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        5 -> {
                            // Step 5: Fast First Win
                            BennuCharacter(
                                stage = BennuStage.CHICK,
                                size = 160.dp,
                                isCelebrating = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "طائر Nour انطلق بنجاح! 🐣✨",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = BennuGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "مبروك يا ${userName.ifBlank { "بطل" }}! حصلت على أول +30 XP كهدية انطلاق 🚀",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextGold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاهز تدخل مساحتك الشخصية اللي هتحتفل بيك في كل خطوة؟",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1 && step < 5) {
                    TextButton(
                        onClick = { step-- },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(text = "السابق", color = TextSecondary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Button(
                    onClick = {
                        if (step < 5) {
                            step++
                        } else {
                            onComplete(userName, selectedHabits, userLatitude, userLongitude)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BennuGold,
                        contentColor = Color(0xFF201300)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (step == 5) "ابدأ Fucos الآن 🔥" else if (step == 1) "ابدأ الرحلة ✨" else "التالي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
