package com.example.bennu

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class ParsedIntent {
    data class AddTask(val title: String, val isExam: Boolean, val priority: String, val dateDesc: String) : ParsedIntent()
    data class AddHabit(val title: String, val icon: String) : ParsedIntent()
    data class AddWorkout(val title: String, val type: String, val durationMinutes: Int) : ParsedIntent()
    data class GeneralChat(val responseText: String) : ParsedIntent()
}

object BennuAIAssistant {

    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_INSTRUCTION = """
أنت «Nour-lv1» — المساعد الذكي وماسكوت تطبيق «Fucos» للطلاب والتركيز وتتبع العادات. رفيق رقمي ذكي مشجع، يرمز للنور والتجدد والتركيز العميق كل يوم.
شخصيتك:
- تتكلم بالعامية المصرية الدافئة، المرحة، المشجعة (زي: "يا فنان"، "يا بطل"، "عاش يا كبير"، "ولا تشيل هم").
- لست روبوتي ولا رسمي، ولا توجّه أي لوم أو تأنيب إطلاقاً لو قصّر الطالب.
- التطبيق مبني على 4 ركائز: المذاكرة 📚، الصلاة 🕌، التمرين 🏋️، العادات ✅.
- شجع الطالب دايماً بأسلوب مبهج ومحبب، وركز على إحياء العادات المهملة والمستمرة.
- إذا طلب إضافة مهمة أو عادة أو تمرين، أكد له ذلك بحماس بأسلوب مصري جميل.
- احرص دايماً على ردود قصيرة ومركزة ومبهجة (سطرين أو ثلاثة سطور بالكثير).
"""

    suspend fun chat(
        userMessage: String,
        contextInfo: String = ""
    ): Pair<String, ParsedIntent?> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        // First, check if natural language command matches an actionable intent
        val localIntent = parseCommandIntent(userMessage)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback response
            val localResponse = generateLocalResponse(userMessage, localIntent, contextInfo)
            return@withContext Pair(localResponse, localIntent)
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            val promptWithContext = if (contextInfo.isNotBlank()) {
                                "سياق نشاط الطالب الحالي: $contextInfo\nرسالة الطالب: $userMessage"
                            } else {
                                userMessage
                            }
                            put(JSONObject().apply { put("text", promptWithContext) })
                        })
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", SYSTEM_INSTRUCTION) })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    return@withContext Pair(text.trim(), localIntent)
                }
            }
        } catch (e: Exception) {
            // In case of network error, fallback gracefully
        }

        // Fallback
        val localFallback = generateLocalResponse(userMessage, localIntent, contextInfo)
        Pair(localFallback, localIntent)
    }

    private fun parseCommandIntent(text: String): ParsedIntent? {
        val lower = text.trim()
        val isAdd = lower.contains("ضيف") || lower.contains("سجل") || lower.contains("عندي") || lower.contains("حط")

        // Exam intent
        if (lower.contains("امتحان") || lower.contains("فاينل") || lower.contains("ميدترم") || lower.contains("كويز")) {
            val title = cleanTitle(lower, listOf("ضيف", "سجل", "عندي", "ميعاد", "جدول"))
            return ParsedIntent.AddTask(title = title, isExam = true, priority = "URGENT", dateDesc = "قريب")
        }

        // Habit intent
        if (lower.contains("عادة") || lower.contains("عاده") || lower.contains("شرب مية") || lower.contains("قراءة") || lower.contains("نوم")) {
            val title = cleanTitle(lower, listOf("ضيف", "سجل", "عادة", "عاده", "جديدة"))
            val icon = when {
                title.contains("مية") || title.contains("ماء") -> "💧"
                title.contains("قراءة") || title.contains("كتاب") -> "📖"
                title.contains("نوم") -> "🌙"
                title.contains("أسنان") || title.contains("سنان") -> "🪥"
                else -> "✨"
            }
            return ParsedIntent.AddHabit(title = title.ifBlank { "عادة يومية جديدة" }, icon = icon)
        }

        // Workout intent
        if (lower.contains("تمرين") || lower.contains("جيم") || lower.contains("ضغط") || lower.contains("كارديو") || lower.contains("جري")) {
            val title = cleanTitle(lower, listOf("ضيف", "سجل", "عملت", "تمرين"))
            val type = if (lower.contains("جيم")) "GYM" else "HOME"
            return ParsedIntent.AddWorkout(title = title.ifBlank { "تمرين بطل" }, type = type, durationMinutes = 20)
        }

        // Task intent
        if (isAdd || lower.contains("واجب") || lower.contains("شيت") || lower.contains("تقرير") || lower.contains("مذاكرة")) {
            val title = cleanTitle(lower, listOf("ضيف", "سجل", "عندي", "لازم أعمل", "مهمة", "واجب"))
            val isUrgent = lower.contains("ضروري") || lower.contains("مهم") || lower.contains("طوارئ")
            return ParsedIntent.AddTask(
                title = title.ifBlank { "مهمة دراسية" },
                isExam = false,
                priority = if (isUrgent) "URGENT" else "NORMAL",
                dateDesc = "اليوم"
            )
        }

        return null
    }

    private fun cleanTitle(original: String, stopWords: List<String>): String {
        var res = original
        for (w in stopWords) {
            res = res.replace(w, "")
        }
        return res.trim().replace(Regex("^[,\\s-]+"), "").replace(Regex("[,\\s-]+$"), "")
    }

    private fun generateLocalResponse(userMessage: String, intent: ParsedIntent?, contextInfo: String): String {
        return when (intent) {
            is ParsedIntent.AddTask -> {
                if (intent.isExam) {
                    "عيوني يا بطل! سجلتلك '${intent.title}' في جدول الامتحانات ووضع الطوارئ 📝 ركز من دلوقتي وهتكسر الدنيا إن شاء الله! 🔥"
                } else {
                    "تسلم يا فنان! أضفت لك مهمة '${intent.title}' في قائمة المذاكرة 📚 خطوة بخطوة وهنخلص كل الواجبات سوا!"
                }
            }
            is ParsedIntent.AddHabit -> {
                "يا سلام على الانضباط! أضفتلك عادة '${intent.title}' ${intent.icon} في ركن العادات.. الاستمرار اليومي هو اللي بيصنع العباقرة ✨"
            }
            is ParsedIntent.AddWorkout -> {
                "عاش يا وحش! سجلتلك تمرين '${intent.title}' 💪 جسمك هو عتادك، والنشاط بيفتح مخك للمذاكرة والتركيز!"
            }
            is ParsedIntent.GeneralChat -> {
                intent.responseText
            }
            null -> {
                when {
                    userMessage.contains("ملخص") || userMessage.contains("صباح") -> {
                        "صباح الورد والنشاط يا بطل! ☀️ رتبتلك اليوم: راجع مهام المذاكرة، واكسب الـ XP في مواعيد الصلاة، وركز في 25 دقيقة بومودورو. أنا في ضهرك دايماً!"
                    }
                    userMessage.contains("شجعني") || userMessage.contains("تعبان") || userMessage.contains("زهقان") -> {
                        "ولا يهمك يا فنان! كل الكبار مروا بلحظات فتور، الفرق إنك تاخد نَفَس عميق وتعمل حاجة واحدة صغيرة بس دلوقتي. إنت قدها وNour-lv1 فخور بيك! 🌟🔥"
                    }
                    userMessage.contains("صلاة") -> {
                        "الصلاة نور وراحة بال يا صديقي 🕌 اضغط علامة الصح جنب كل فرض وهتاخد XP يكبّر مستواك ويخلي يومك بركة ✨"
                    }
                    userMessage.contains("شكرا") || userMessage.contains("حبيبي") -> {
                        "حبيبي يا بطل! ده واجبي ووجودي هنا عشان نوصل سوا للقمة والتركيز.. ارتقِ كل يوم خطوة! 🌟"
                    }
                    else -> {
                        "منور يا غالي! قولي محتاج إيه؟ أنا Nour-lv1 أقدر أضيفلك مهام، امتحانات، عادات، أو أحسبلك بومودورو.. إنت تأمر وأنا أنفذ! 🦅"
                    }
                }
            }
        }
    }
}
