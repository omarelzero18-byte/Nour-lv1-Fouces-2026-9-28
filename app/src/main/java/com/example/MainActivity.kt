package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bennu.BennuCharacter
import com.example.bennu.BennuStage
import com.example.ui.MainViewModel
import com.example.ui.components.WeeklyWrappedDialog
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Enforce Native Arabic RTL Layout across entire application
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    IrtiqaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun IrtiqaApp(viewModel: MainViewModel) {
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val showWeeklyWrapped by viewModel.showWeeklyWrapped.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val currentStage = BennuStage.fromXp(progress.totalXp)

    // If onboarding is not completed, show onboarding flow
    if (!progress.onboardingCompleted) {
        OnboardingScreen(
            onComplete = { name, starterHabits, lat, lon ->
                viewModel.completeOnboarding(name, starterHabits, lat, lon)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBackground),
            containerColor = MidnightBackground,
            contentWindowInsets = WindowInsets.systemBars,
            floatingActionButton = {
                // Floating Nour-lv1 AI Quick Assistant
                FloatingActionButton(
                    onClick = { viewModel.selectTab(5) }, // Go to Nour-lv1 Chat
                    containerColor = BennuGold,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 60.dp)
                        .testTag("nour_fab_chat")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "كلّم Nour-lv1", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MidnightSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .border(1.dp, MidnightCardBorderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    val navItems = listOf(
                        Triple(0, "اليوم", Icons.Default.Home),
                        Triple(1, "المذاكرة", Icons.AutoMirrored.Filled.MenuBook),
                        Triple(2, "الصلاة", Icons.Default.Mosque),
                        Triple(3, "التمرين", Icons.Default.FitnessCenter),
                        Triple(4, "العادات", Icons.Default.CheckCircle),
                        Triple(5, "Nour-lv1", Icons.Default.AutoAwesome)
                    )

                    navItems.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) BennuGold else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) BennuGold else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x33FFB300)
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(viewModel = viewModel)
                    1 -> StudyScreen(viewModel = viewModel)
                    2 -> PrayerScreen(viewModel = viewModel)
                    3 -> WorkoutScreen(viewModel = viewModel)
                    4 -> HabitsScreen(viewModel = viewModel)
                    5 -> BennuChatScreen(viewModel = viewModel)
                }
            }
        }

        // Spotify Wrapped Style Weekly Celebration Dialog
        if (showWeeklyWrapped) {
            WeeklyWrappedDialog(
                stats = weeklyStats,
                userName = progress.userName,
                stage = currentStage,
                onDismiss = { viewModel.closeWeeklyWrapped() }
            )
        }
    }
}
