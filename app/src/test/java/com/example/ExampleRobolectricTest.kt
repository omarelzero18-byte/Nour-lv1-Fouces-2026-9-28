package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.bennu.BennuStage
import com.example.util.PrayerTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fucos", appName)
  }

  @Test
  fun `verify prayer times calculator produces valid 5 daily prayers`() {
    val result = PrayerTimesCalculator.calculate(date = Date())
    assertNotNull(result.fajr)
    assertNotNull(result.dhuhr)
    assertNotNull(result.asr)
    assertNotNull(result.maghrib)
    assertNotNull(result.isha)
    assertEquals("الفجر", result.fajr.nameArabic)
    assertEquals("الظهر", result.dhuhr.nameArabic)
    assertEquals("العصر", result.asr.nameArabic)
    assertEquals("المغرب", result.maghrib.nameArabic)
    assertEquals("العشاء", result.isha.nameArabic)
    assertTrue(result.fajr.hour in 3..7)
    assertTrue(result.dhuhr.hour in 11..14)
  }

  @Test
  fun `verify bennu evolution stage progression`() {
    assertEquals(BennuStage.EGG, BennuStage.fromXp(50))
    assertEquals(BennuStage.CHICK, BennuStage.fromXp(150))
    assertEquals(BennuStage.YOUNG_BIRD, BennuStage.fromXp(400))
    assertEquals(BennuStage.RADIANT_PHOENIX, BennuStage.fromXp(850))
  }
}
