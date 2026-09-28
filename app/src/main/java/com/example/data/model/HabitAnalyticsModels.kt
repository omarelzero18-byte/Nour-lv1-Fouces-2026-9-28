package com.example.data.model

import com.example.data.local.entity.HabitEntity

data class HabitPerformance(
    val habit: HabitEntity,
    val streak: Int,
    val completionsLast7Days: Int,
    val completionRatePercent: Int
)

data class HabitNeglectInfo(
    val habit: HabitEntity,
    val daysSinceLastCompleted: Int,
    val completionsLast30Days: Int,
    val statusLabel: String
)

data class NeglectAlert(
    val habitTitle: String,
    val habitIcon: String,
    val daysNeglected: Int,
    val motivationalPrompt: String
)

data class HabitAnalytics(
    val overallProgressPercent: Int = 0,
    val totalCompletions7Days: Int = 0,
    val topConsistentHabits: List<HabitPerformance> = emptyList(),
    val mostNeglectedHabits: List<HabitNeglectInfo> = emptyList(),
    val neglectAlert: NeglectAlert? = null
)
