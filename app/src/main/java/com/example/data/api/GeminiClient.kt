package com.example.data.api

import android.util.Log
import com.example.data.model.LessonData
import com.example.data.model.LessonQuestion
import com.example.data.model.PairItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateLesson(
        targetLanguage: String,
        nativeLanguage: String,
        unit: Int,
        level: Int,
        userApiKey: String
    ): LessonData? = withContext(Dispatchers.IO) {
        // Use the supplied user key, or fallback to the BuildConfig injected key if available!
        val keyToUse = userApiKey.ifEmpty { 
            try {
                // Accessing key securely injected via Secrets Plugin
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

        if (keyToUse.isEmpty() || keyToUse == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key supplied or found.")
            return@withContext null
        }

        val prompt = """
            You are an expert language teacher for Infinity Academy (a gamified language learning app like Duolingo).
            Generate a highly engaging, gamified language learning lesson for someone whose native language is "$nativeLanguage" and who wants to learn "$targetLanguage".
            The lesson should be appropriate for Unit $unit, Level $level.
            Provide exactly 5 unique, high-quality interactive questions of varying types.
            
            The output MUST be a single raw JSON object matching the following structure:
            {
              "title": "A short engaging lesson title (e.g. Greeting Basics)",
              "description": "Short description of what the user will learn in this lesson",
              "unit": $unit,
              "level": $level,
              "questions": [
                {
                  "type": "MULTIPLE_CHOICE",
                  "prompt": "Translate this phrase or word",
                  "targetText": "Phrase in $targetLanguage",
                  "correctAnswer": "Correct translation in $nativeLanguage",
                  "options": ["Correct translation in $nativeLanguage", "Wrong translation 1", "Wrong translation 2", "Wrong translation 3"]
                },
                {
                  "type": "WORD_BANK",
                  "prompt": "Translate the sentence into $nativeLanguage",
                  "targetText": "Sentence in $targetLanguage",
                  "correctAnswer": "Correct full sentence in $nativeLanguage",
                  "words": ["Correct", "words", "shuffled", "plus", "extra", "words", "in", "$nativeLanguage"]
                },
                {
                  "type": "PAIR_MATCHING",
                  "prompt": "Match these words with their translations",
                  "pairs": [
                    {"left": "$targetLanguage word 1", "right": "$nativeLanguage translation 1"},
                    {"left": "$targetLanguage word 2", "right": "$nativeLanguage translation 2"},
                    {"left": "$targetLanguage word 3", "right": "$nativeLanguage translation 3"},
                    {"left": "$targetLanguage word 4", "right": "$nativeLanguage translation 4"}
                  ]
                }
              ]
            }
            
            Strict requirements:
            1. Provide a mix of question types: "MULTIPLE_CHOICE", "WORD_BANK", "PAIR_MATCHING", and "TRANSLATE_TYPING".
            2. For "MULTIPLE_CHOICE", provide exactly 4 options. Shuffled.
            3. For "WORD_BANK", the "words" array must contain all the words of the "correctAnswer" split up, plus 3-4 extra distraction words in "$nativeLanguage".
            4. For "PAIR_MATCHING", provide exactly 4 pairs.
            5. Ensure all translation answers are completely accurate. If the native language is Bengali, write accurate Bengali words and sentences.
            6. Return ONLY the raw JSON object. Do not wrap in markdown tags like ```json or ```. No extra text before or after.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val url = "$BASE_URL?key=$keyToUse"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code ${response.code}")
                    return@withContext null
                }
                val bodyString = response.body?.string() ?: return@withContext null
                Log.d(TAG, "Response: $bodyString")

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.getJSONArray("candidates")
                val textResponse = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Clean the response text from any markdown code blocks
                var cleanJson = textResponse.trim()
                if (cleanJson.startsWith("```json")) {
                    cleanJson = cleanJson.substringAfter("```json")
                } else if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.substringAfter("```")
                }
                if (cleanJson.endsWith("```")) {
                    cleanJson = cleanJson.substringBeforeLast("```")
                }
                cleanJson = cleanJson.trim()

                // Parse the clean JSON into our LessonData domain object manually
                val lessonObj = JSONObject(cleanJson)
                val title = lessonObj.getString("title")
                val description = lessonObj.getString("description")
                val lessonUnit = lessonObj.getInt("unit")
                val lessonLevel = lessonObj.getInt("level")
                
                val qArray = lessonObj.getJSONArray("questions")
                val questionsList = mutableListOf<LessonQuestion>()

                for (i in 0 until qArray.length()) {
                    val qObj = qArray.getJSONObject(i)
                    val qType = qObj.getString("type")
                    val qPrompt = qObj.getString("prompt")
                    
                    var targetText = ""
                    var correctAnswer = ""
                    val options = mutableListOf<String>()
                    val words = mutableListOf<String>()
                    val pairs = mutableListOf<PairItem>()

                    if (qObj.has("targetText")) targetText = qObj.getString("targetText")
                    if (qObj.has("correctAnswer")) correctAnswer = qObj.getString("correctAnswer")

                    if (qObj.has("options")) {
                        val optArr = qObj.getJSONArray("options")
                        for (j in 0 until optArr.length()) {
                            options.add(optArr.getString(j))
                        }
                    }

                    if (qObj.has("words")) {
                        val wordArr = qObj.getJSONArray("words")
                        for (j in 0 until wordArr.length()) {
                            words.add(wordArr.getString(j))
                        }
                    }

                    if (qObj.has("pairs")) {
                        val pairArr = qObj.getJSONArray("pairs")
                        for (j in 0 until pairArr.length()) {
                            val pObj = pairArr.getJSONObject(j)
                            pairs.add(PairItem(pObj.getString("left"), pObj.getString("right")))
                        }
                    }

                    questionsList.add(
                        LessonQuestion(
                            type = qType,
                            prompt = qPrompt,
                            targetText = targetText,
                            correctAnswer = correctAnswer,
                            options = options,
                            words = words,
                            pairs = pairs
                        )
                    )
                }

                LessonData(
                    title = title,
                    description = description,
                    unit = lessonUnit,
                    level = lessonLevel,
                    questions = questionsList
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini content generation", e)
            null
        }
    }
}
