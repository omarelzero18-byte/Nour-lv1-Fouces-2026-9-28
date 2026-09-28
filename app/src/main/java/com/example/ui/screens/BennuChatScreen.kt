package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.bennu.BennuCharacter
import com.example.bennu.BennuStage
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun BennuChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val currentStage = BennuStage.fromXp(progress.totalXp)

    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Android Speech to Text Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputQuery = spokenText
                viewModel.sendChatMessage(spokenText)
                inputQuery = ""
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "ملخصي الصباحي ☀️",
        "شجعني بكلمتين يا Nour-lv1 🔥",
        "ضيف امتحان فيزياء الأسبوع الجاي 📝",
        "سجل تمرين ضغط 20 عدة 💪",
        "ضيف عادة قراءة 10 دقايق 📖"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, BennuGold, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nour_avatar),
                    contentDescription = "Nour-lv1 Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nour-lv1 — رفيقك الذكي ✨",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BennuGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrayerGreen)
                    )
                }
                Text(
                    text = "اكتب أو اتكلم، وبيسجل مهامك ويرتب يومك تلقائياً",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // --- Quick Prompts Row ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidnightSurfaceVariant)
                        .border(1.dp, MidnightCardBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { viewModel.sendChatMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = prompt, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                }
            }
        }

        // --- Chat Messages List ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, BennuGold, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.nour_avatar),
                                contentDescription = "Nour-lv1 Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "أهلاً يا ${progress.userName}! قولي محتاج إيه؟",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BennuGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "أنا Nour-lv1، أقدر أضيفلك امتحانات، مهام، عادات، وأساعدك تركز في ركائزك اليومية! ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
        }

        // --- Input Bar (Text + Voice + Send) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speech-to-Text Mic Button
            IconButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "اتكلم مع Nour-lv1.. هيفهمك فوراً ✨")
                    }
                    try {
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Fallback if STT is unavailable
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MidnightSurfaceVariant)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "تسجيل صوتي", tint = BennuGold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input
            OutlinedTextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("اكتب رسالتك لـ Nour-lv1...", color = TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BennuGold,
                    unfocusedBorderColor = MidnightCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = MidnightSurface,
                    unfocusedContainerColor = MidnightSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (inputQuery.isNotBlank()) {
                        viewModel.sendChatMessage(inputQuery)
                        inputQuery = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BennuGold)
                    .testTag("chat_send_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.Black)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 4.dp else 18.dp,
                        bottomEnd = if (isUser) 18.dp else 4.dp
                    )
                )
                .background(
                    if (isUser) Color(0xFF1E293B) else Color(0xFF1C1917)
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) Color(0x3338BDF8) else BennuGold.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 4.dp else 18.dp,
                        bottomEnd = if (isUser) 18.dp else 4.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.nour_avatar),
                            contentDescription = "Nour-lv1",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Nour-lv1", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = BennuGold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = TextPrimary
                )
            }
        }
    }
}
