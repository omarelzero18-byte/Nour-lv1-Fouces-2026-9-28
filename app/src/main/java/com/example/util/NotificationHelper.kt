package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "irtiqa_notifications"
    const val CHANNEL_NAME = "تنبيهات Fucos والصلاة"

    const val ACTION_PRAYER = "com.example.ACTION_PRAYER_ALERT"
    const val ACTION_MORNING_BRIEF = "com.example.ACTION_MORNING_BRIEF"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "مواقيت الصلاة والملخص الصباحي وتنبيهات العادات مع Nour-lv1"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showPrayerNotification(context: Context, prayerName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد صلاة $prayerName 🕌")
            .setContentText("«أقم صلاتك يصفُ قلبك وتتيسر أمورك.. ركز وانطلق الآن»")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(prayerName.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showMorningBriefNotification(context: Context, userName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("صباح الخير يا $userName! ☀️🦅")
            .setContentText("Nour-lv1 رتّب لك مهام المذاكرة والعادات اليومية.. جاهز ليوم عظيم من التركيز؟")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(999, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showNeglectHabitNotification(context: Context, habitTitle: String, daysNeglected: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("تنبيه عادة من Nour-lv1 🌟")
            .setContentText("عادة '$habitTitle' متوقفة بقالها $daysNeglected أيام.. خطوة واحدة اليوم ترجعك للمسار!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(habitTitle.hashCode(), notification)
        } catch (_: SecurityException) {}
    }

    fun scheduleDailyAlarms(context: Context, prayerTimes: PrayerTimesResult, userName: String = "بطل Fucos") {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        val prayers = listOf(
            prayerTimes.fajr,
            prayerTimes.dhuhr,
            prayerTimes.asr,
            prayerTimes.maghrib,
            prayerTimes.isha
        )

        for ((index, pray) in prayers.withIndex()) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, pray.hour)
                set(Calendar.MINUTE, pray.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (cal.timeInMillis > now) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = ACTION_PRAYER
                    putExtra(EXTRA_PRAYER_NAME, pray.nameArabic)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    100 + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis,
                        pendingIntent
                    )
                } catch (e: Exception) {
                    try {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                    } catch (_: Exception) {}
                }
            }
        }

        // Morning brief at 8:00 AM
        val morningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (morningCal.timeInMillis > now) {
            val morningIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_MORNING_BRIEF
                putExtra("user_name", userName)
            }
            val morningPending = PendingIntent.getBroadcast(
                context,
                200,
                morningIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    morningCal.timeInMillis,
                    morningPending
                )
            } catch (e: Exception) {
                try {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, morningCal.timeInMillis, morningPending)
                } catch (_: Exception) {}
            }
        }
    }
}
