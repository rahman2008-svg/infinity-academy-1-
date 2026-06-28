package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AcademyDatabase
import com.example.data.database.AcademyRepository
import com.example.data.database.LeaderboardUser
import com.example.data.database.UserStats
import com.example.data.database.VocabularyWord
import com.example.data.model.LessonData
import com.example.data.model.LessonQuestion
import com.example.data.model.PairItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    ONBOARDING,
    MAIN_DASHBOARD,
    ACTIVE_LESSON,
    LESSON_SUMMARY
}

enum class DashboardTab {
    PATH,
    VOCABULARY,
    LEADERBOARD,
    PROFILE
}

class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AcademyDatabase.getDatabase(application)
    private val repository = AcademyRepository(db.academyDao())

    // --- Core Database Flows ---
    val userStats: StateFlow<UserStats?> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardUser>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vocabulary: StateFlow<List<VocabularyWord>> = repository.vocabulary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Screen Navigation State ---
    var currentScreen by mutableStateOf(AppScreen.ONBOARDING)
        private set

    var currentTab by mutableStateOf(DashboardTab.PATH)

    // --- Onboarding Selection States ---
    var selectedTargetLanguage by mutableStateOf("Spanish")
    var selectedNativeLanguage by mutableStateOf("English")
    var selectedDailyGoalXP by mutableStateOf(20) // 20 XP casual, 50 XP serious
    var inputtedApiKey by mutableStateOf("")

    // --- Active Lesson Play States ---
    var activeLesson: LessonData? by mutableStateOf(null)
        private set
    var currentQuestionIndex by mutableStateOf(0)
    var selectedAnswerIndex by mutableStateOf(-1) // MULTIPLE_CHOICE
    var selectedWords = mutableStateOf<List<String>>(emptyList()) // WORD_BANK
    var remainingWords = mutableStateOf<List<String>>(emptyList()) // WORD_BANK Shuffled
    var enteredText by mutableStateOf("") // TRANSLATE_TYPING
    
    // PAIR_MATCHING specific state
    var selectedLeftPair by mutableStateOf<PairItem?>(null)
    var selectedRightPair by mutableStateOf<PairItem?>(null)
    var matchedPairs = mutableStateOf<Set<PairItem>>(emptySet())
    var wrongPairAttempts = mutableStateOf<Set<PairItem>>(emptySet())

    var hearts by mutableStateOf(5)
    var showFeedback by mutableStateOf(false)
    var isCorrectFeedback by mutableStateOf(false)
    var correctFeedbackText by mutableStateOf("")

    // Lesson Finish Stats
    var lessonXPEarned by mutableStateOf(0)
    var lessonGemsEarned by mutableStateOf(0)
    var lessonSuccessAccuracy by mutableStateOf(100)
    private var lessonTotalQuestions = 5
    private var lessonCorrectFirstTryCount = 0
    private var hasMistakeOnCurrentQuestion = false

    // --- TTS Engine ---
    private var tts: TextToSpeech? = null

    init {
        try {
            tts = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    setTtsLanguage()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelScope.launch {
            // Check if stats exist, create if empty
            val stats = repository.getOrCreateUserStats()
            if (stats.onboarded) {
                currentScreen = AppScreen.MAIN_DASHBOARD
                inputtedApiKey = stats.apiKey
                selectedTargetLanguage = stats.targetLanguage
                selectedNativeLanguage = stats.nativeLanguage
            } else {
                currentScreen = AppScreen.ONBOARDING
            }
            
            // Set up a dynamic leaderboard
            repository.setupDefaultLeaderboard(stats.xp)
        }
    }

    // --- Actions ---

    fun completeOnboarding() {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats().copy(
                targetLanguage = selectedTargetLanguage,
                nativeLanguage = selectedNativeLanguage,
                onboarded = true,
                apiKey = inputtedApiKey
            )
            repository.updateUserStats(stats)
            currentScreen = AppScreen.MAIN_DASHBOARD
        }
    }

    fun saveApiKey(newKey: String) {
        inputtedApiKey = newKey
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats().copy(apiKey = newKey)
            repository.updateUserStats(stats)
        }
    }

    fun changeLanguagePair(target: String, native: String) {
        selectedTargetLanguage = target
        selectedNativeLanguage = native
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats().copy(
                targetLanguage = target,
                nativeLanguage = native
            )
            repository.updateUserStats(stats)
        }
    }

    // --- Lesson Play Core ---

    fun startLesson(unit: Int, level: Int) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            hearts = stats.hearts
            if (hearts <= 0) {
                // Instantly refill hearts so they can play! (Duolingo hack - super friendly)
                hearts = 5
                repository.updateUserStats(stats.copy(hearts = 5))
            }

            // Fetch lesson dynamically (via Gemini if Key, otherwise Preloaded)
            val lesson = repository.getLesson(
                target = stats.targetLanguage,
                native = stats.nativeLanguage,
                unit = unit,
                level = level,
                userApiKey = stats.apiKey
            )
            
            activeLesson = lesson
            currentQuestionIndex = 0
            lessonTotalQuestions = lesson.questions.size
            lessonCorrectFirstTryCount = 0
            hasMistakeOnCurrentQuestion = false
            resetQuestionInteractions()
            currentScreen = AppScreen.ACTIVE_LESSON
        }
    }

    private fun resetQuestionInteractions() {
        selectedAnswerIndex = -1
        enteredText = ""
        selectedWords.value = emptyList()
        selectedLeftPair = null
        selectedRightPair = null
        matchedPairs.value = emptySet()
        wrongPairAttempts.value = emptySet()
        showFeedback = false
        hasMistakeOnCurrentQuestion = false

        // For Word Bank, shuffle the answer words plus some random ones
        val currentQuestion = activeLesson?.questions?.getOrNull(currentQuestionIndex) ?: return
        if (currentQuestion.type == "WORD_BANK") {
            remainingWords.value = currentQuestion.words.shuffled()
        }
    }

    // Interactive selections
    fun selectMultipleChoiceAnswer(index: Int) {
        if (!showFeedback) {
            selectedAnswerIndex = index
        }
    }

    fun tapWordInBank(word: String, isSelected: Boolean) {
        if (showFeedback) return
        if (isSelected) {
            // Remove from selected
            selectedWords.value = selectedWords.value - word
            remainingWords.value = remainingWords.value + word
        } else {
            // Add to selected
            selectedWords.value = selectedWords.value + word
            remainingWords.value = remainingWords.value - word
        }
    }

    fun selectPairLeft(pair: PairItem) {
        if (showFeedback || matchedPairs.value.contains(pair)) return
        selectedLeftPair = pair
        checkPairMatch()
    }

    fun selectPairRight(pair: PairItem) {
        if (showFeedback) return
        // Find which pair this right side matches with
        val matchingPair = activeLesson?.questions?.getOrNull(currentQuestionIndex)?.pairs?.find { it.right == pair.right } ?: return
        if (matchedPairs.value.contains(matchingPair)) return
        selectedRightPair = matchingPair
        checkPairMatch()
    }

    private fun checkPairMatch() {
        val left = selectedLeftPair
        val right = selectedRightPair
        if (left != null && right != null) {
            if (left == right) {
                // MATCHED!
                matchedPairs.value = matchedPairs.value + left
                selectedLeftPair = null
                selectedRightPair = null
                wrongPairAttempts.value = emptySet()

                // Check if all matched
                val totalPairs = activeLesson?.questions?.getOrNull(currentQuestionIndex)?.pairs?.size ?: 0
                if (matchedPairs.value.size == totalPairs) {
                    // Instantly show success!
                    isCorrectFeedback = true
                    correctFeedbackText = "Awesome matching!"
                    showFeedback = true
                }
            } else {
                // WRONG MATCH
                wrongPairAttempts.value = setOf(left, right)
                selectedLeftPair = null
                selectedRightPair = null
            }
        }
    }

    // Submit and Check Answer
    fun submitAnswer() {
        if (showFeedback) return
        val question = activeLesson?.questions?.getOrNull(currentQuestionIndex) ?: return

        isCorrectFeedback = false
        correctFeedbackText = ""

        when (question.type) {
            "MULTIPLE_CHOICE" -> {
                if (selectedAnswerIndex == -1) return
                val answer = question.options.getOrNull(selectedAnswerIndex)
                if (answer == question.correctAnswer) {
                    isCorrectFeedback = true
                } else {
                    correctFeedbackText = "Correct Answer: ${question.correctAnswer}"
                }
            }
            "TRANSLATE_TYPING" -> {
                val answer = enteredText.trim().lowercase()
                val correct = question.correctAnswer.trim().lowercase()
                if (answer == correct || answer.contains(correct) || correct.contains(answer) && answer.length > 2) {
                    isCorrectFeedback = true
                } else {
                    correctFeedbackText = "Correct Answer: ${question.correctAnswer}"
                }
            }
            "WORD_BANK" -> {
                val fullAnswer = selectedWords.value.joinToString(" ").trim().lowercase().replace(",", "").replace(".", "").replace("?", "")
                val correct = question.correctAnswer.trim().lowercase().replace(",", "").replace(".", "").replace("?", "")
                if (fullAnswer == correct || fullAnswer.replace(" ", "") == correct.replace(" ", "")) {
                    isCorrectFeedback = true
                } else {
                    correctFeedbackText = "Correct: ${question.correctAnswer}"
                }
            }
            "PAIR_MATCHING" -> {
                // Already auto-checked as they matched, but fallback
                val totalPairs = question.pairs.size
                if (matchedPairs.value.size == totalPairs) {
                    isCorrectFeedback = true
                }
            }
        }

        if (isCorrectFeedback) {
            if (!hasMistakeOnCurrentQuestion) {
                lessonCorrectFirstTryCount++
            }
            // Save words to Vocabulary table so the user can review later!
            saveVocabularyFromQuestion(question)
        } else {
            hearts = (hearts - 1).coerceAtLeast(0)
            hasMistakeOnCurrentQuestion = true
            viewModelScope.launch {
                val stats = repository.getOrCreateUserStats().copy(hearts = hearts)
                repository.updateUserStats(stats)
            }
        }

        showFeedback = true
    }

    private fun saveVocabularyFromQuestion(question: LessonQuestion) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            val pairName = "${stats.targetLanguage}-${stats.nativeLanguage}"
            if (question.type == "MULTIPLE_CHOICE" || question.type == "TRANSLATE_TYPING") {
                repository.saveWord(
                    targetWord = question.targetText,
                    nativeTranslation = question.correctAnswer,
                    pronunciationHint = "Spoken in ${stats.targetLanguage}",
                    pair = pairName
                )
            } else if (question.type == "PAIR_MATCHING") {
                question.pairs.forEach {
                    repository.saveWord(
                        targetWord = it.left,
                        nativeTranslation = it.right,
                        pronunciationHint = "Key Term",
                        pair = pairName
                    )
                }
            }
        }
    }

    // Advance to next question or complete lesson
    fun nextQuestion() {
        if (hearts <= 0) {
            // Failed lesson, go back to dashboard
            currentScreen = AppScreen.MAIN_DASHBOARD
            return
        }

        val questions = activeLesson?.questions ?: return
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            resetQuestionInteractions()
        } else {
            // Complete Lesson! Gained 25 XP and 15 Gems
            val xpGain = 25
            val gemsGain = 15
            lessonXPEarned = xpGain
            lessonGemsEarned = gemsGain
            lessonSuccessAccuracy = ((lessonCorrectFirstTryCount.toFloat() / lessonTotalQuestions.toFloat()) * 100).toInt()

            viewModelScope.launch {
                repository.addXPAndGems(xpGain, gemsGain)
                
                // Update stats unit/level
                val stats = repository.getOrCreateUserStats()
                val nextLevel = if (stats.currentLevel < 10) stats.currentLevel + 1 else 1
                val nextUnit = if (nextLevel == 1) stats.currentUnit + 1 else stats.currentUnit
                repository.updateUserStats(stats.copy(
                    currentUnit = nextUnit,
                    currentLevel = nextLevel
                ))
            }
            currentScreen = AppScreen.LESSON_SUMMARY
        }
    }

    fun exitLesson() {
        currentScreen = AppScreen.MAIN_DASHBOARD
    }

    fun changeUnit(newUnit: Int) {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            repository.updateUserStats(stats.copy(
                currentUnit = newUnit,
                currentLevel = 1
            ))
        }
    }

    fun refillHearts() {
        viewModelScope.launch {
            val stats = repository.getOrCreateUserStats()
            if (stats.gems >= 100) {
                repository.updateUserStats(stats.copy(
                    gems = stats.gems - 100,
                    hearts = 5
                ))
                hearts = 5
            }
        }
    }

    fun setTtsLanguage() {
        val locale = when (selectedTargetLanguage) {
            "Spanish" -> Locale("es", "ES")
            "French" -> Locale.FRANCE
            "German" -> Locale.GERMANY
            "Italian" -> Locale.ITALY
            "Portuguese" -> Locale("pt", "PT")
            "Japanese" -> Locale.JAPAN
            "Korean" -> Locale.KOREA
            "Chinese" -> Locale.CHINA
            "Hindi" -> Locale("hi", "IN")
            "English" -> Locale.US
            else -> Locale.US
        }
        try {
            tts?.language = locale
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun speak(text: String) {
        setTtsLanguage()
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
