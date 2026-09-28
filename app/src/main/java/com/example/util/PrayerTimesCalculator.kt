package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

data class PrayerTime(
    val nameArabic: String,
    val key: String,
    val hour: Int,
    val minute: Int,
    val formattedTime: String
)

data class PrayerTimesResult(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val currentPrayer: PrayerTime?,
    val nextPrayer: PrayerTime?,
    val minutesUntilNext: Long
)

object PrayerTimesCalculator {

    // Default coordinates: Cairo, Egypt (Center of Bennu's heritage)
    const val DEFAULT_LATITUDE = 30.0444
    const val DEFAULT_LONGITUDE = 31.2357
    const val DEFAULT_TIMEZONE = 2.0 // or 3.0 during summer, dynamically calculated

    fun calculate(
        date: Date = Date(),
        latitude: Double = DEFAULT_LATITUDE,
        longitude: Double = DEFAULT_LONGITUDE,
        timeZoneOffsetHours: Double = (Calendar.getInstance().timeZone.getOffset(date.time) / 3600000.0)
    ): PrayerTimesResult {
        val calendar = Calendar.getInstance().apply { time = date }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Approximate equation of time and declination of the Sun
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eotMinutes = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)
        val declinationDeg = 23.45 * sin(2.0 * Math.PI * (284 + dayOfYear) / 365.0)
        val declinationRad = Math.toRadians(declinationDeg)
        val latRad = Math.toRadians(latitude)

        val deviceTz = Calendar.getInstance().timeZone.getOffset(date.time) / 3600000.0
        val effectiveTz = if (deviceTz == 0.0) (longitude / 15.0).roundToInt().toDouble() else deviceTz
        val tzHours = if (timeZoneOffsetHours != 0.0) timeZoneOffsetHours else effectiveTz

        // Solar Noon (in decimal hours)
        val solarNoon = 12.0 - (longitude / 15.0 - tzHours) - (eotMinutes / 60.0)

        // Helper to calculate hour angle for a given zenith angle
        fun getHourAngle(angleDeg: Double): Double {
            val angleRad = Math.toRadians(angleDeg)
            val cosHA = (sin(angleRad) - sin(latRad) * sin(declinationRad)) / (cos(latRad) * cos(declinationRad))
            if (cosHA > 1.0 || cosHA < -1.0) return 0.0
            return Math.toDegrees(acos(cosHA)) / 15.0
        }

        // Egyptian General Authority of Survey angles:
        // Fajr: -19.5 deg, Isha: -17.5 deg
        // Horizon dip with refraction: -0.833 deg
        val sunriseAngle = -0.833
        val fajrAngle = -19.5
        val ishaAngle = -17.5

        val sunriseHA = getHourAngle(sunriseAngle)
        val fajrHA = getHourAngle(fajrAngle)
        val ishaHA = getHourAngle(ishaAngle)

        // Asr Calculation (Standard Shadow = 1 + noon shadow)
        val noonAltitudeRad = (Math.PI / 2.0) - abs(latRad - declinationRad)
        val noonShadow = 1.0 / tan(noonAltitudeRad)
        val asrAltitudeRad = atan(1.0 / (1.0 + noonShadow))
        val asrAngleDeg = Math.toDegrees(asrAltitudeRad)
        val asrHA = getHourAngle(asrAngleDeg)

        val fajrHours = solarNoon - fajrHA
        val sunriseHours = solarNoon - sunriseHA
        val dhuhrHours = solarNoon
        val asrHours = solarNoon + asrHA
        val maghribHours = solarNoon + sunriseHA
        val ishaHours = solarNoon + ishaHA

        fun createPrayerTime(nameArabic: String, key: String, decHours: Double): PrayerTime {
            var norm = decHours % 24.0
            if (norm < 0) norm += 24.0
            val h = norm.toInt()
            val m = ((norm - h) * 60).roundToInt().coerceIn(0, 59)
            val period = if (h >= 12) "م" else "ص"
            val displayH = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", displayH, m, period)
            return PrayerTime(nameArabic, key, h, m, formatted)
        }

        val fajr = createPrayerTime("الفجر", "fajr", fajrHours)
        val sunrise = createPrayerTime("الشروق", "sunrise", sunriseHours)
        val dhuhr = createPrayerTime("الظهر", "dhuhr", dhuhrHours)
        val asr = createPrayerTime("العصر", "asr", asrHours)
        val maghrib = createPrayerTime("المغرب", "maghrib", maghribHours)
        val isha = createPrayerTime("العشاء", "isha", ishaHours)

        val prayerList = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        var currentPrayer: PrayerTime? = null
        var nextPrayer: PrayerTime? = null
        var minutesUntilNext: Long = 0

        for (i in prayerList.indices) {
            val p = prayerList[i]
            val pMinutes = p.hour * 60 + p.minute
            if (currentMinutes >= pMinutes) {
                currentPrayer = p
            } else {
                nextPrayer = p
                minutesUntilNext = (pMinutes - currentMinutes).toLong()
                break
            }
        }

        if (nextPrayer == null) {
            // Next is tomorrow's Fajr
            nextPrayer = fajr
            val fajrMinutes = fajr.hour * 60 + fajr.minute
            minutesUntilNext = (24 * 60 - currentMinutes + fajrMinutes).toLong()
        }
        if (currentPrayer == null) {
            currentPrayer = isha
        }

        return PrayerTimesResult(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            minutesUntilNext = minutesUntilNext
        )
    }

    private val dailyWisdoms = listOf(
        "«رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي» 🌿",
        "«اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا» ✨",
        "«وَقُلْ رَبِّ زِدْنِي عِلْمًا» 📖",
        "«اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا» 🤲",
        "«اسْتَعِنْ بِاللَّهِ وَلَا تَعْجَزْ» — خُطوة ورا خطوة والخير قادم 💫",
        "«إِنَّ مَعَ الْعُسْرِ يُسْرًا» — رتّب وقتك وخلّي أملك كبير 🌤️",
        "«أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ» 🌱"
    )

    fun getDailyWisdom(): String {
        val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) % dailyWisdoms.size
        return dailyWisdoms[dayIndex]
    }
}
