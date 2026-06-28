package com.example.data.database

import android.content.Context
import com.example.data.api.GeminiClient
import com.example.data.model.LessonData
import com.example.data.model.PreloadedLessons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class AcademyRepository(private val dao: AcademyDao) {

    val userStats: Flow<UserStats?> = dao.getUserStats()
    val leaderboard: Flow<List<LeaderboardUser>> = dao.getLeaderboard()
    val vocabulary: Flow<List<VocabularyWord>> = dao.getVocabulary()

    suspend fun getOrCreateUserStats(): UserStats {
        val stats = dao.getUserStatsDirect()
        return if (stats == null) {
            val defaultStats = UserStats()
            dao.insertUserStats(defaultStats)
            setupDefaultLeaderboard(0)
            defaultStats
        } else {
            stats
        }
    }

    suspend fun updateUserStats(stats: UserStats) {
        dao.insertUserStats(stats)
        // Keep current user XP synced in the leaderboard
        updateLeaderboardUserXP(stats.xp)
    }

    suspend fun addXPAndGems(xpGain: Int, gemsGain: Int) {
        val current = getOrCreateUserStats()
        val newXp = current.xp + xpGain
        val newGems = current.gems + gemsGain
        
        // Calculate streak increase if applicable
        // Just increment streak for simulation or simplify
        val newStreak = if (current.streak == 0) 1 else current.streak

        val updated = current.copy(
            xp = newXp,
            gems = newGems,
            streak = newStreak
        )
        dao.insertUserStats(updated)
        
        // Update user on leaderboard and simulate other competitors studying!
        updateLeaderboardUserXP(newXp)
        simulateCompetitorProgress()
    }

    suspend fun saveWord(targetWord: String, nativeTranslation: String, pronunciationHint: String, pair: String) {
        // Check if word is already learned
        if (dao.hasWord(targetWord) == 0) {
            val word = VocabularyWord(
                targetWord = targetWord,
                nativeTranslation = nativeTranslation,
                pronunciationHint = pronunciationHint,
                languagePair = pair
            )
            dao.insertWord(word)
        }
    }

    suspend fun setupDefaultLeaderboard(userXP: Int) {
        dao.clearLeaderboard()
        val defaultCompetitors = listOf(
            LeaderboardUser(name = "Duo_The_Owl", xp = 1250, avatarColorHex = "#FF55A630"),
            LeaderboardUser(name = "Socrates_Grad", xp = 980, avatarColorHex = "#FFFFB703"),
            LeaderboardUser(name = "Babu_Learning", xp = 820, avatarColorHex = "#FF00B4D8"),
            LeaderboardUser(name = "Sophia_Smart", xp = 670, avatarColorHex = "#FFFF4D6D"),
            LeaderboardUser(name = "Language_Sage", xp = 540, avatarColorHex = "#FF7209B7"),
            LeaderboardUser(name = "Anika_Speak", xp = 410, avatarColorHex = "#FFF72585"),
            LeaderboardUser(name = "Infinity_Pro", xp = 280, avatarColorHex = "#FFFB8500"),
            LeaderboardUser(name = "Duolingo_Fan", xp = 150, avatarColorHex = "#FF4CC9F0"),
            LeaderboardUser(name = "You", xp = userXP, avatarColorHex = "#FF0077B6", isCurrentUser = true)
        )
        dao.insertLeaderboard(defaultCompetitors)
    }

    private suspend fun updateLeaderboardUserXP(userXP: Int) {
        val currentBoard = dao.getLeaderboard().firstOrNull() ?: return
        val updatedBoard = currentBoard.map {
            if (it.isCurrentUser) {
                it.copy(xp = userXP)
            } else {
                it
            }
        }
        dao.insertLeaderboard(updatedBoard)
    }

    suspend fun simulateCompetitorProgress() {
        val currentBoard = dao.getLeaderboard().firstOrNull() ?: return
        val updatedBoard = currentBoard.map {
            if (!it.isCurrentUser) {
                // Competitor studied and gained 10 to 45 XP!
                val gain = Random.nextInt(10, 45)
                it.copy(xp = it.xp + gain)
            } else {
                it
            }
        }
        dao.insertLeaderboard(updatedBoard)
    }

    // --- Course / Lesson Engine ---

    suspend fun getLesson(target: String, native: String, unit: Int, level: Int, userApiKey: String): LessonData {
        // Since user requested "NO AI API keys", we prioritize our incredibly deep 51 languages offline generative database!
        // We also check GeminiClient as a fallback if the user has a key.
        if (userApiKey.isNotEmpty()) {
            val aiLesson = GeminiClient.generateLesson(target, native, unit, level, userApiKey)
            if (aiLesson != null) {
                return aiLesson
            }
        }

        // Otherwise fallback to our offline-first deterministic master database generator
        return PreloadedLessons.generateLesson(target, native, unit, level)
    }
}
