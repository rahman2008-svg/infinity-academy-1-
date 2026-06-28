package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.HeartRed
import com.example.ui.theme.IncorrectRed
import com.example.ui.viewmodel.AcademyViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActiveLessonScreen(viewModel: AcademyViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val lesson = viewModel.activeLesson ?: return
    val currentQuestion = lesson.questions.getOrNull(viewModel.currentQuestionIndex) ?: return

    var studyModeActive by remember { mutableStateOf(true) }
    LaunchedEffect(lesson) {
        studyModeActive = true
    }

    if (studyModeActive) {
        var selectedTab by remember { mutableIntStateOf(0) }
        var isRecording by remember { mutableStateOf(false) }
        var micFeedback by remember { mutableStateOf("") }
        var activePlayingAudioId by remember { mutableStateOf(-1) }
        val isBengali = stats?.nativeLanguage == "Bengali"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 88.dp)
            ) {
                // Header Bar (Exit button, Lesson title)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.exitLesson() }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit", tint = MaterialTheme.colorScheme.onBackground)
                    }

                    Text(
                        text = if (isBengali) "পাঠ প্রস্তুতি (Study Prep)" else "Lesson Prep & Study",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Lesson Title Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💡", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = lesson.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isBengali) "কুইজের আগে শব্দভাণ্ডার এবং বাক্যগুলি রিভিশন দিন।" else "Learn vocabulary & phrases before the test.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Material 3 Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isBengali) "📝 শব্দ ও বাক্য" else "📝 Words & Phrases", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isBengali) "🖼️ ছবি ও অডিও" else "🖼️ Visual & Audio", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (isBengali) "💡 ব্যাকরণ ও উচ্চারণ" else "💡 Grammar & Voice", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }

                // Tab Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Vocabulary & Sentences
                            val scroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scroll),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "১৫টি নতুন শব্দ (ট্যাপ করে শুনুন)" else "Vocabulary (15 new words) - Tap to listen",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                // Words grid or list
                                lesson.vocabularyWords.forEachIndexed { index, pair ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.speak(pair.left) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pair.left, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                Text(pair.right, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Listen",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = if (isBengali) "১০টি বাক্য (ট্যাপ করে শুনুন)" else "Conversation Sentences (10 sentences)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                lesson.sentences.forEachIndexed { index, pair ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.speak(pair.left) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pair.left, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                Text(pair.right, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Icon(
                                                Icons.Default.VolumeUp,
                                                contentDescription = "Listen",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        1 -> {
                            // Visuals & Audios
                            val scroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scroll),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "৫টি চিত্র কার্ড" else "Visual Cards (5 images)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    lesson.visualCards.take(2).forEach { card ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.speak(card.left) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val emoji = when (card.right.lowercase()) {
                                                    in listOf("house", "বাড়ি") -> "🏠"
                                                    in listOf("water", "জল / পানি") -> "💧"
                                                    in listOf("bread", "রুটি") -> "🍞"
                                                    in listOf("milk", "দুধ") -> "🥛"
                                                    in listOf("book", "বই") -> "📖"
                                                    in listOf("cat", "বিড়াল") -> "🐱"
                                                    in listOf("dog", "কুকুর") -> "🐶"
                                                    in listOf("love", "ভালোবাসা") -> "❤️"
                                                    in listOf("happy", "খুশি") -> "😊"
                                                    in listOf("sun", "সূর্য") -> "☀️"
                                                    in listOf("moon", "চাঁদ") -> "🌙"
                                                    in listOf("coffee", "কফি") -> "☕"
                                                    in listOf("tea", "চা") -> "🍵"
                                                    in listOf("friend", "বন্ধু") -> "🤝"
                                                    in listOf("school", "বিদ্যালয় / স্কুল") -> "🏫"
                                                    else -> "🌟"
                                                }
                                                Text(emoji, fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
                                                Text(card.left, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                                Text(card.right, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    lesson.visualCards.drop(2).take(2).forEach { card ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.speak(card.left) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                val emoji = when (card.right.lowercase()) {
                                                    in listOf("house", "বাড়ি") -> "🏠"
                                                    in listOf("water", "জল / পানি") -> "💧"
                                                    in listOf("bread", "রুটি") -> "🍞"
                                                    in listOf("milk", "দুধ") -> "🥛"
                                                    in listOf("book", "বই") -> "📖"
                                                    in listOf("cat", "বিড়াল") -> "🐱"
                                                    in listOf("dog", "কুকুর") -> "🐶"
                                                    in listOf("love", "ভালোবাসা") -> "❤️"
                                                    in listOf("happy", "খুশি") -> "😊"
                                                    in listOf("sun", "সূর্য") -> "☀️"
                                                    in listOf("moon", "চাঁদ") -> "🌙"
                                                    in listOf("coffee", "কফি") -> "☕"
                                                    in listOf("tea", "চা") -> "🍵"
                                                    in listOf("friend", "বন্ধু") -> "🤝"
                                                    in listOf("school", "বিদ্যালয় / স্কুল") -> "🏫"
                                                    else -> "🌟"
                                                }
                                                Text(emoji, fontSize = 48.sp, modifier = Modifier.padding(bottom = 8.dp))
                                                Text(card.left, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                                Text(card.right, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = if (isBengali) "৫টি শোনার পরীক্ষা (অডিও ল্যাব)" else "Listening Lab (5 audios)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                lesson.audioCards.forEachIndexed { idx, pair ->
                                    val isPlaying = activePlayingAudioId == idx
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                activePlayingAudioId = idx
                                                viewModel.speak(pair.left)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(1.dp, if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = "Play",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = pair.left,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(pair.right, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            if (isPlaying) {
                                                Text(if (isBengali) "🔊 চলছে" else "🔊 Playing", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        2 -> {
                            // Grammar & Pronunciation Practice
                            val scroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scroll),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = if (isBengali) "১টি ব্যাকরণ টিপ" else "Grammar Corner (1 grammar tip)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💡", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(if (isBengali) "ব্যাকরণ বিধি ও টিপস" else "Grammar Rule & Tip", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = lesson.grammarTip,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }

                                Text(
                                    text = if (isBengali) "১টি উচ্চারণ অনুশীলন" else "Pronunciation Check (1 pronunciation check)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 16.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isBengali) "মাইক্রোফোন ট্যাপ করে নিচের বাক্যটি বলুন:" else "Tap microphone and speak this sentence:",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = lesson.pronunciationPrompt,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))

                                        IconButton(
                                            onClick = {
                                                if (!isRecording) {
                                                    isRecording = true
                                                    micFeedback = if (isBengali) "শুনছি... কথা বলুন" else "Listening... Speak now"
                                                    viewModel.speak(lesson.pronunciationPrompt)
                                                } else {
                                                    isRecording = false
                                                    micFeedback = if (isBengali) "চমৎকার! নিখুঁত উচ্চারণ! 🎯" else "Excellent! Perfect Accent! 🎯"
                                                }
                                            },
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(
                                                    if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                                                contentDescription = "Speak",
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        if (micFeedback.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = micFeedback,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isRecording) MaterialTheme.colorScheme.error else CorrectGreen,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Sticky "Start Quiz" Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.95f), MaterialTheme.colorScheme.background)
                        )
                    )
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        studyModeActive = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_quiz_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isBengali) "কুইজ শুরু করুন ➔" else "Ready to Quiz? Let's Go! ➔",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    val totalQuestions = lesson.questions.size
    val progress = (viewModel.currentQuestionIndex.toFloat() / totalQuestions.toFloat())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // Leave space for check/feedback bar
        ) {
            // Header Bar (Close button, Progress bar, Hearts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.exitLesson() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Hearts",
                        tint = HeartRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.hearts}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = HeartRed
                    )
                }
            }

            // Question Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mascot Bubble Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Mascot Face
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "Mascot Helper",
                            modifier = Modifier.fillMaxSize().padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Speech bubble card
                    Card(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = currentQuestion.prompt,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Target Text Display (the prompt sentence they must translate)
                if (currentQuestion.targetText.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = currentQuestion.targetText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Answer Options / Inputs based on Question Type
                when (currentQuestion.type) {
                    "MULTIPLE_CHOICE" -> {
                        currentQuestion.options.forEachIndexed { idx, option ->
                            val isSelected = viewModel.selectedAnswerIndex == idx
                            OutlinedCard(
                                onClick = { viewModel.selectMultipleChoiceAnswer(idx) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .testTag("choice_$idx"),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = option,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }

                    "TRANSLATE_TYPING" -> {
                        OutlinedTextField(
                            value = viewModel.enteredText,
                            onValueChange = { viewModel.enteredText = it },
                            placeholder = { Text("Type translation...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("typing_input"),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !viewModel.showFeedback,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }

                    "WORD_BANK" -> {
                        // SELECTED WORDS BOARD (Where they build their translated sentence)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .padding(bottom = 16.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (viewModel.selectedWords.value.isEmpty()) {
                                    Text(
                                        text = "Tap words below to arrange...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    viewModel.selectedWords.value.forEach { word ->
                                        WordCard(word = word) {
                                            viewModel.tapWordInBank(word, isSelected = true)
                                        }
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                        // REMAINING WORD BANK (Shuffled clickable word cards)
                        Text(
                            text = "Word Bank (শব্দ সংকলন)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.remainingWords.value.forEach { word ->
                                WordCard(word = word) {
                                    viewModel.tapWordInBank(word, isSelected = false)
                                }
                            }
                        }
                    }

                    "PAIR_MATCHING" -> {
                        // 2 columns (Left column: target language, Right column: native language)
                        val pairs = currentQuestion.pairs
                        val totalPairsMatched = viewModel.matchedPairs.value.size
                        
                        Text(
                            text = "Pairs matched: $totalPairsMatched / ${pairs.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left Target Language Column
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pairs.forEach { pair ->
                                    val isMatched = viewModel.matchedPairs.value.contains(pair)
                                    val isSelected = viewModel.selectedLeftPair == pair
                                    val isWrong = viewModel.wrongPairAttempts.value.contains(pair)

                                    PairCard(
                                        text = pair.left,
                                        isSelected = isSelected,
                                        isMatched = isMatched,
                                        isWrong = isWrong,
                                        modifier = Modifier.testTag("pair_left_${pair.left}")
                                    ) {
                                        viewModel.selectPairLeft(pair)
                                    }
                                }
                            }

                            // Right Native Language Column
                            // Shuffled or displayed (we use their items matched)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pairs.sortedBy { it.right }.forEach { pair ->
                                    val isMatched = viewModel.matchedPairs.value.contains(pair)
                                    val isSelected = viewModel.selectedRightPair == pair
                                    val isWrong = viewModel.wrongPairAttempts.value.contains(pair)

                                    PairCard(
                                        text = pair.right,
                                        isSelected = isSelected,
                                        isMatched = isMatched,
                                        isWrong = isWrong,
                                        modifier = Modifier.testTag("pair_right_${pair.right}")
                                    ) {
                                        viewModel.selectPairRight(pair)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // HEARTS DEAD / LESSON FAILED DIALOG STATE
        if (viewModel.hearts <= 0) {
            AlertDialog(
                onDismissRequest = { viewModel.exitLesson() },
                title = { Text("Hearts Depleted! (জীবন শেষ)", fontWeight = FontWeight.Black) },
                text = {
                    Text("You ran out of hearts. No problem, mistakes are how we learn! Refill 5 hearts for 100 gems, or return to the learning path.")
                },
                confirmButton = {
                    val canAfford = (stats?.gems ?: 0) >= 100
                    Button(
                        onClick = { viewModel.refillHearts() },
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Refill Hearts (100 Gems)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.exitLesson() }) {
                        Text("Exit Path", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            )
        }

        // Submitting / Checking Answer Bottom Feedback Panel
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 8.dp,
            color = if (viewModel.showFeedback) {
                if (viewModel.isCorrectFeedback) CorrectGreen.copy(alpha = 0.12f) else IncorrectRed.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (viewModel.showFeedback) {
                    // Correct/Incorrect visual indicator text & details
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isCorrectFeedback) CorrectGreen else IncorrectRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (viewModel.isCorrectFeedback) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = "Result",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (viewModel.isCorrectFeedback) {
                                    listOf("Brilliant!", "Excellent!", "Spot on!", "Superb!", "Awesome!").random()
                                } else {
                                    "Correction Required"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (viewModel.isCorrectFeedback) CorrectGreen else IncorrectRed
                            )
                            if (viewModel.correctFeedbackText.isNotEmpty()) {
                                Text(
                                    text = viewModel.correctFeedbackText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("lesson_continue_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isCorrectFeedback) CorrectGreen else IncorrectRed
                        )
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Check button inactive state
                    val hasAnswer = when (currentQuestion.type) {
                        "MULTIPLE_CHOICE" -> viewModel.selectedAnswerIndex != -1
                        "TRANSLATE_TYPING" -> viewModel.enteredText.isNotEmpty()
                        "WORD_BANK" -> viewModel.selectedWords.value.isNotEmpty()
                        "PAIR_MATCHING" -> viewModel.matchedPairs.value.size == currentQuestion.pairs.size
                        else -> false
                    }

                    Button(
                        onClick = { viewModel.submitAnswer() },
                        enabled = hasAnswer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("lesson_submit_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Check Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WordCard(word: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = word,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PairCard(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    isWrong: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = when {
        isMatched -> CorrectGreen.copy(alpha = 0.12f)
        isWrong -> IncorrectRed.copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isMatched -> CorrectGreen
        isWrong -> IncorrectRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }

    val borderWidth = if (isSelected || isMatched || isWrong) 2.dp else 1.dp

    OutlinedCard(
        onClick = { if (!isMatched) onClick() },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        enabled = !isMatched
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isMatched -> CorrectGreen
                    isWrong -> IncorrectRed
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
