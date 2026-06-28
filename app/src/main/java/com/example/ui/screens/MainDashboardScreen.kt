package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GemBlue
import com.example.ui.theme.GoldXP
import com.example.ui.theme.HeartRed
import com.example.ui.viewmodel.AcademyViewModel
import com.example.ui.viewmodel.DashboardTab
import com.example.data.model.PreloadedLessons
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(viewModel: AcademyViewModel) {
    val stats by viewModel.userStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Language Selector Capsule
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            onClick = { viewModel.currentTab = DashboardTab.PROFILE }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stats?.nativeLanguage ?: "EN"} ➔ ${stats?.targetLanguage ?: "ES"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Stats counters (Streak, Gems, Hearts)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Streak count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFFF9600), // Fire Orange
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${stats?.streak ?: 0}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFFFF9600)
                                )
                            }

                            // Gems count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Diamond,
                                    contentDescription = "Gems",
                                    tint = GemBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${stats?.gems ?: 0}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = GemBlue
                                )
                            }

                            // Hearts count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Hearts",
                                    tint = HeartRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${stats?.hearts ?: 5}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = HeartRed
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = viewModel.currentTab == DashboardTab.PATH,
                    onClick = { viewModel.currentTab = DashboardTab.PATH },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Path") },
                    label = { Text("Learn") },
                    modifier = Modifier.testTag("nav_tab_path")
                )
                NavigationBarItem(
                    selected = viewModel.currentTab == DashboardTab.VOCABULARY,
                    onClick = { viewModel.currentTab = DashboardTab.VOCABULARY },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Vocabulary") },
                    label = { Text("Review") },
                    modifier = Modifier.testTag("nav_tab_vocabulary")
                )
                NavigationBarItem(
                    selected = viewModel.currentTab == DashboardTab.LEADERBOARD,
                    onClick = { viewModel.currentTab = DashboardTab.LEADERBOARD },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Leaderboard") },
                    modifier = Modifier.testTag("nav_tab_leaderboard")
                )
                NavigationBarItem(
                    selected = viewModel.currentTab == DashboardTab.PROFILE,
                    onClick = { viewModel.currentTab = DashboardTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (viewModel.currentTab) {
                DashboardTab.PATH -> PathTabContent(viewModel)
                DashboardTab.VOCABULARY -> VocabularyTabContent(viewModel)
                DashboardTab.LEADERBOARD -> LeaderboardTabContent(viewModel)
                DashboardTab.PROFILE -> ProfileTabContent(viewModel)
            }
        }
    }
}

// --- Tab 1: Path / Learn ---
@Composable
fun PathTabContent(viewModel: AcademyViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // We display a beautiful zig-zag path of levels representing the gamified path.
    // Each level matches user's unit progression.
    val currentLevel = stats?.currentLevel ?: 1
    val currentUnit = stats?.currentUnit ?: 1

    val sectionId = ((currentUnit - 1) / 25) + 1
    val unitInSection = ((currentUnit - 1) % 25) + 1
    
    val sectionTitles = listOf(
        "Foundations & Greetings" to "শুভেচ্ছা ও প্রাথমিক আলোচনা",
        "Family & Friends" to "পরিবার ও বন্ধুবান্ধব",
        "Food & Drinks" to "খাদ্য ও পানীয়",
        "Daily Life & Routine" to "দৈনন্দিন জীবন ও রুটিন",
        "Places & Travel" to "স্থান ও ভ্রমণ",
        "Shopping & Clothes" to "কেনাকাটা ও পোশাক",
        "Numbers & Time" to "সংখ্যা ও সময়",
        "Activities & Hobbies" to "কাজ ও শখ",
        "Nature & Weather" to "প্রকৃতি ও আবহাওয়া",
        "Conversations & Stories" to "কথোপকথন ও গল্প"
    )
    val selectedSectionPair = sectionTitles.getOrElse(sectionId - 1) { sectionTitles.first() }
    val isBengali = stats?.nativeLanguage == "Bengali"
    val sectionTitleName = if (isBengali) selectedSectionPair.second else selectedSectionPair.first

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Unit Banner Block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = if (isBengali) "সেকশন $sectionId • ইউনিট $unitInSection" else "SECTION $sectionId • UNIT $unitInSection",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sectionTitleName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isBengali) "সহজ ও মজার উপায়ে নিখুঁতভাবে ভাষা শিখুন।" else "Learn to speak fluently and construct solid initial expressions.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // API Key Badge Info
                    val hasKey = stats?.apiKey?.isNotEmpty() == true
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (hasKey) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.clickable { viewModel.currentTab = DashboardTab.PROFILE }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (hasKey) Icons.Default.AutoAwesome else Icons.Default.Lock,
                                contentDescription = "AI Mode",
                                tint = if (hasKey) Color.White else MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasKey) "AI Infinite Lessons Active" else "Local Practice Mode (Tap to Unlock AI)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasKey) Color.White else MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.2f))

                    // Interactive Navigational buttons to explore all 250 Units!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (currentUnit > 1) {
                                    viewModel.changeUnit(currentUnit - 1)
                                }
                            },
                            enabled = currentUnit > 1,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.3f))
                        ) {
                            Text("◀ " + (if (isBengali) "পূর্ববর্তী" else "Prev"))
                        }
                        
                        Text(
                            text = if (isBengali) "ইউনিট $currentUnit / ২৫০" else "Unit $currentUnit / 250",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        TextButton(
                            onClick = {
                                if (currentUnit < 250) {
                                    viewModel.changeUnit(currentUnit + 1)
                                }
                            },
                            enabled = currentUnit < 250,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.3f))
                        ) {
                            Text((if (isBengali) "পরবর্তী" else "Next") + " ▶")
                        }
                    }
                }
            }
        }

        // Zig-Zag Snake Nodes
        // Let's draw 10 nodes (representing the 10 Lessons of the Unit)
        val totalNodes = 10
        items(totalNodes) { index ->
            val nodeLevel = index + 1
            // Determine position multiplier for zig-zag (-1 to 1)
            val angle = (index * 0.9f)
            val horizontalOffset = (sin(angle) * 80).dp

            val isActive = nodeLevel == currentLevel
            val isCompleted = nodeLevel < currentLevel
            val isLocked = nodeLevel > currentLevel

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = horizontalOffset)
                        .size(if (isActive) 88.dp else 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Active Pulsing Border Ring
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        )
                    }

                    // The actual Circular Level Button
                    IconButton(
                        onClick = {
                            if (!isLocked) {
                                viewModel.startLesson(currentUnit, nodeLevel)
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> MaterialTheme.colorScheme.primary
                                    isCompleted -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                }
                            )
                            .testTag("path_level_node_$nodeLevel")
                    ) {
                        Icon(
                            imageVector = when {
                                isCompleted -> Icons.Default.Check
                                isLocked -> Icons.Default.Lock
                                else -> Icons.Default.PlayArrow
                            },
                            contentDescription = "Level $nodeLevel",
                            tint = if (isLocked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Level numbering bubble
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "$nodeLevel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }

                // Node Title / Label
                Text(
                    text = when (nodeLevel) {
                        1 -> "Greetings"
                        2 -> "Family Members"
                        3 -> "Food & Drinks"
                        4 -> "Common Places"
                        5 -> "Shopping Time"
                        6 -> "Colors & Clothes"
                        7 -> "Action Verbs"
                        else -> "Simple Dialogues"
                    },
                    modifier = Modifier
                        .offset(x = horizontalOffset)
                        .padding(top = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// --- Tab 2: Vocabulary / Review ---
@Composable
fun VocabularyTabContent(viewModel: AcademyViewModel) {
    val vocabList by viewModel.vocabulary.collectAsState()

    if (vocabList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = "Book",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Your Vocabulary is empty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Complete your very first lesson node to automatically gather learned words, sentence examples, and definitions here!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Vocabulary Vault (শব্দ ভাণ্ডার)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 4.dp)
            )
            Text(
                text = "You have unlocked ${vocabList.size} terms from your active sessions. Practice these to lock them into memory!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vocabList) { word ->
                    var isPronounced by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.targetWord,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Translation: ${word.nativeTranslation}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isPronounced) {
                                    Text(
                                        text = "Pronunciation: /${word.pronunciationHint}/",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isPronounced = !isPronounced },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isPronounced) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                )
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = if (isPronounced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Tab 3: Leaderboard / Competitive Tournament ---
@Composable
fun LeaderboardTabContent(viewModel: AcademyViewModel) {
    val competitors by viewModel.leaderboard.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tournament Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sapphire League Tournament",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(
                    text = "Gain XP from lessons to advance up the rankings. Top 3 competitors promote to Ruby League!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Competitors List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(competitors) { index, competitor ->
                val rank = index + 1
                val isSelf = competitor.isCurrentUser

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelf) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            colors = if (isSelf) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            else listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ),
                        width = if (isSelf) 2.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Number badge
                            Text(
                                text = "$rank",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = when (rank) {
                                    1 -> Color(0xFFFFB703) // Gold
                                    2 -> Color(0xFFCCCCCC) // Silver
                                    3 -> Color(0xFFCD7F32) // Bronze
                                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                },
                                modifier = Modifier.width(28.dp)
                            )

                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(competitor.avatarColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = competitor.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = if (isSelf) "${competitor.name} (You)" else competitor.name,
                                fontWeight = if (isSelf) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Score Block
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${competitor.xp}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "XP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Tab 4: Profile / Settings Tab ---
@Composable
fun ProfileTabContent(viewModel: AcademyViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val vocabList by viewModel.vocabulary.collectAsState()
    val uriHandler = LocalUriHandler.current

    val targetLanguages = PreloadedLessons.languages
    val nativeLanguages = PreloadedLessons.languages

    var showTargetDropdown by remember { mutableStateOf(false) }
    var showNativeDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Infinity Scholar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Registered Scholar since 2026",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(
                            icon = Icons.Default.LocalFireDepartment,
                            value = "${stats?.streak ?: 0}",
                            label = "Day Streak",
                            color = Color(0xFFFF9600)
                        )
                        ProfileStatItem(
                            icon = Icons.Default.Bolt,
                            value = "${stats?.xp ?: 0}",
                            label = "Total XP",
                            color = GoldXP
                        )
                        ProfileStatItem(
                            icon = Icons.Default.School,
                            value = "${vocabList.size}",
                            label = "Words",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Settings / Customization Block
        item {
            Text(
                text = "Course Configuration",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Native Language Selector
                    Text(
                        text = "My Native Language",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)) {
                        OutlinedButton(
                            onClick = { showNativeDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stats?.nativeLanguage ?: "English", fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Open")
                            }
                        }
                        DropdownMenu(
                            expanded = showNativeDropdown,
                            onDismissRequest = { showNativeDropdown = false }
                        ) {
                            nativeLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        viewModel.changeLanguagePair(stats?.targetLanguage ?: "Spanish", lang)
                                        showNativeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Target Language Selector
                    Text(
                        text = "Language to Learn",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        OutlinedButton(
                            onClick = { showTargetDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stats?.targetLanguage ?: "Spanish", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Open")
                            }
                        }
                        DropdownMenu(
                            expanded = showTargetDropdown,
                            onDismissRequest = { showTargetDropdown = false }
                        ) {
                            targetLanguages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        viewModel.changeLanguagePair(lang, stats?.nativeLanguage ?: "English")
                                        showTargetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // API Key Section
        item {
            Text(
                text = "Gemini AI Engine Integration",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            var keyInput by remember { mutableStateOf(stats?.apiKey ?: "") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configure API key to unlock the Gemini Infinite Generator. When configured, Infinity Academy will dynamically construct unique tests tailored to your exact progression on ANY language pair!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("AI_STUDIO_KEY_...") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_api_key_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.saveApiKey(keyInput) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save and Update Key")
                    }
                }
            }
        }

        // --- About Developer Section ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "About Developer",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Developer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Prince AR Abdur Rahman",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Independent App Developer",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Connect & Contact",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact Links
                    ContactLinkRow(
                        icon = Icons.Default.Phone,
                        label = "WhatsApp: 01707424006",
                        onClick = { uriHandler.openUri("https://wa.me/8801707424006") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ContactLinkRow(
                        icon = Icons.Default.Phone,
                        label = "WhatsApp: 01796951709",
                        onClick = { uriHandler.openUri("https://wa.me/8801796951709") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ContactLinkRow(
                        icon = Icons.Default.Share,
                        label = "Facebook Profile",
                        value = "https://www.facebook.com/share/1BNn32qoJo/",
                        onClick = { uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ContactLinkRow(
                        icon = Icons.Default.Share,
                        label = "Instagram Profile",
                        value = "https://www.instagram.com/ur___abdur____rahman__2008",
                        onClick = { uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008") }
                    )
                }
            }
        }

        // --- About Company Section ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "About Company",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = "Company",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NexVora Lab's Ofc",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Innovative App Publisher",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mission block
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Our Mission",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- Technical Info & Credits Section ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Technical Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "App Version", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(text = "1.0.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Credits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developed by Prince AR Abdur Rahman",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Published by NexVora Lab's Ofc",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ContactLinkRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (value != null) {
                    Text(
                        text = value,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Open Link",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ProfileStatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
    }
}
