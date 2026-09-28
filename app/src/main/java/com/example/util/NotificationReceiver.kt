package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationHelper.ACTION_PRAYER -> {
                val prayerName = intent.getStringExtra(NotificationHelper.EXTRA_PRAYER_NAME) ?: "الصلاة"
                NotificationHelper.showPrayerNotification(context, prayerName)
            }
            NotificationHelper.ACTION_MORNING_BRIEF -> {
                val userName = intent.getStringExtra("user_name") ?: "بطل Fucos"
                NotificationHelper.showMorningBriefNotification(context, userName)
            }
        }
    }
}
