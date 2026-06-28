package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 0,
    val targetLanguage: String = "Spanish",
    val nativeLanguage: String = "English",
    val xp: Int = 0,
    val gems: Int = 500,
    val streak: Int = 0,
    val hearts: Int = 5,
    val currentUnit: Int = 1,
    val currentLevel: Int = 1,
    val onboarded: Boolean = false,
    val apiKey: String = "",
    val lastLessonDate: String = "" // "YYYY-MM-DD" to check streak completion
)

@Entity(tableName = "leaderboard")
data class LeaderboardUser(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val xp: Int,
    val avatarColorHex: String,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetWord: String,
    val nativeTranslation: String,
    val pronunciationHint: String,
    val languagePair: String,
    val learnedTimestamp: Long = System.currentTimeMillis()
)

// --- DAO ---

@Dao
interface AcademyDao {
    @Query("SELECT * FROM user_stats WHERE id = 0")
    fun getUserStats(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 0")
    suspend fun getUserStatsDirect(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Query("SELECT * FROM leaderboard ORDER BY xp DESC")
    fun getLeaderboard(): Flow<List<LeaderboardUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(users: List<LeaderboardUser>)

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()

    @Query("SELECT * FROM vocabulary ORDER BY learnedTimestamp DESC")
    fun getVocabulary(): Flow<List<VocabularyWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyWord)
    
    @Query("SELECT COUNT(*) FROM vocabulary WHERE targetWord = :word LIMIT 1")
    suspend fun hasWord(word: String): Int
}

// --- Database ---

@Database(
    entities = [UserStats::class, LeaderboardUser::class, VocabularyWord::class],
    version = 1,
    exportSchema = false
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: AcademyDatabase? = null

        fun getDatabase(context: Context): AcademyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "academy_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
