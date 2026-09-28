package com.example.bennu

enum class BennuStage(
    val level: Int,
    val titleArabic: String,
    val descriptionArabic: String,
    val minXp: Int,
    val maxXp: Int
) {
    EGG(
        level = 1,
        titleArabic = "بيضة متوهجة",
        descriptionArabic = "بداية رحلة Fucos والتركيز، النور بيتجمع جواها ✨",
        minXp = 0,
        maxXp = 100
    ),
    CHICK(
        level = 2,
        titleArabic = "طائر Nour الصغير",
        descriptionArabic = "ريشه بيتحرك بحماس، ومستني انضباطك عشان يلمع 🐣",
        minXp = 100,
        maxXp = 300
    ),
    YOUNG_BIRD(
        level = 3,
        titleArabic = "طائر شاب بَريش لامع",
        descriptionArabic = "انضباطك وركائزك الأربعة بدأت تخليه يحلّق بقوة 🦅",
        minXp = 300,
        maxXp = 700
    ),
    RADIANT_PHOENIX(
        level = 4,
        titleArabic = "Nour الأسطوري المكتمل",
        descriptionArabic = "نور دافئ ساطع يرمز لانتظامك الأسطوري وتجددك كل يوم 🔥",
        minXp = 700,
        maxXp = 1500
    );

    companion object {
        fun fromXp(xp: Int): BennuStage {
            return when {
                xp < 100 -> EGG
                xp < 300 -> CHICK
                xp < 700 -> YOUNG_BIRD
                else -> RADIANT_PHOENIX
            }
        }
    }
}

object BennuQuotes {
    val celebrations = listOf(
        "عاش يا فنان! كدة بنقرب خطوة للقمة 🔥",
        "يا سلام عليك يا بطل! Nour-lv1 فخور بيك جداً ✨",
        "الله ينور عليك! إنجاز ورا إنجاز وهتوصل 💪",
        "تسلم إيدك! الالتزام ده هيغير مستقبلك 🚀",
        "حلاوتك يا كبير! كدة أخدت XP وNour-lv1 فرحان بيك 🌟"
    )

    val greetings = listOf(
        "صباحك سكر ورضا! جاهز نبدأ يومنا بتركيز مع Nour-lv1؟ ☀️",
        "منور يا بطل! الركائز الأربعة مستنياك تلمع فيها 💫",
        "ارتقِ وركز كل يوم خطوة.. وNour-lv1 معاك في كل لحظة 🦅",
        "يلا بينا نكتسح المهام النهارده؟ ولا تشيل أي هم! 🌟"
    )

    val gentleNudges = listOf(
        "جاهز نسجل إنك صليت الفرض اللي عليك؟ 🌤️",
        "إيه رأيك في جلسة بومودورو 25 دقيقة بس؟ 📚",
        "شربت مية النهارده؟ جسمك محتاج طاقة يا بطل! 💧",
        "تمرين سريع 15 دقيقة يصحصح جسمك وينشط عقلك؟ 🏋️"
    )

    fun getRandomCelebration(): String = celebrations.random()
    fun getRandomGreeting(): String = greetings.random()
    fun getRandomNudge(): String = gentleNudges.random()
}
