package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.AcademyViewModel
import com.example.data.model.PreloadedLessons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: AcademyViewModel) {
    var step by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    val targetLanguages = PreloadedLessons.languages
    val nativeLanguages = PreloadedLessons.languages

    var showTargetDropdown by remember { mutableStateOf(false) }
    var showNativeDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Title Block
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Infinity Academy",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
                Text(
                    text = "Infinite Language Learning Powered by Gemini AI",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Interactive steps content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    1 -> {
                        // Welcome Graphic & Goal Intro
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_mascot_welcome),
                                contentDescription = "Mascot Welcome",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "স্বাগতম! Welcome to Infinity Academy",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "The ultimate Duolingo clone. Learn any language in the world through gamified lessons. Set up your learning profile to start your infinite path!",
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    2 -> {
                        // Language Selection
                        Text(
                            text = "Select Language Pair",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "What language do you speak, and what would you like to master?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        // NATIVE / SOURCE LANGUAGE DROPDOWN
                        Text(
                            text = "My Native Language (আমি এই ভাষায় কথা বলি)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                            OutlinedCard(
                                onClick = { showNativeDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = viewModel.selectedNativeLanguage,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown")
                                }
                            }
                            DropdownMenu(
                                expanded = showNativeDropdown,
                                onDismissRequest = { showNativeDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                nativeLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            viewModel.selectedNativeLanguage = lang
                                            showNativeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // TARGET LANGUAGE DROPDOWN
                        Text(
                            text = "Language I Want to Learn (আমি যে ভাষা শিখতে চাই)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { showTargetDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = viewModel.selectedTargetLanguage,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown")
                                }
                            }
                            DropdownMenu(
                                expanded = showTargetDropdown,
                                onDismissRequest = { showTargetDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                targetLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            viewModel.selectedTargetLanguage = lang
                                            showTargetDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        if (viewModel.selectedNativeLanguage == viewModel.selectedTargetLanguage) {
                            Text(
                                text = "Please select different languages for a real course experience!",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    3 -> {
                        // Daily Goal
                        Text(
                            text = "Choose Your Daily Goal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "How intense do you want your daily language study to be?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        val goals = listOf(
                            Triple("Casual (সহজ)", 10, "5 mins / day"),
                            Triple("Regular (স্বাভাবিক)", 25, "10 mins / day"),
                            Triple("Serious (কঠিন)", 50, "15 mins / day"),
                            Triple("Insane (পাগলাটে)", 100, "25 mins / day")
                        )

                        goals.forEach { (label, xp, duration) ->
                            val isSelected = viewModel.selectedDailyGoalXP == xp
                            OutlinedCard(
                                onClick = { viewModel.selectedDailyGoalXP = xp },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = if (isSelected) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        else listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    ),
                                    width = if (isSelected) 2.dp else 1.dp
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = duration,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                    Text(
                                        text = "$xp XP",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    4 -> {
                        // AI Key Configuration
                        Text(
                            text = "✨ Unlock AI Infinite Mode",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Optionally insert your Gemini API key to unlock infinite dynamically generated exercises for ANY topic & language!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        OutlinedTextField(
                            value = viewModel.inputtedApiKey,
                            onValueChange = { viewModel.inputtedApiKey = it },
                            label = { Text("Gemini API Key (Optional)") },
                            placeholder = { Text("AI_STUDIO_KEY_...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input"),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "If left empty, Infinity Academy uses high-quality preloaded courses for top pairs like Spanish, Japanese, and English (to and from Bengali!). You can set your key anytime in settings.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress dots indicator
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (step == i + 1) 12.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (step == i + 1) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (step == 4) "Start Learning (শুরু করুন)" else "Continue",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }

                if (step > 1) {
                    TextButton(
                        onClick = { step-- },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Back",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
