package com.example.ataraxia.features.journal.domain

import com.example.ataraxia.data.local.entity.JournalEntryEntity
import java.util.Calendar

data class JournalInsights(
    val totalWords: Int,
    val averageEntryLength: Int,
    val writingFrequency: Int,
    val favoriteWritingTime: String,
    val mostCommonMood: String,
    val longestReflectionWords: Int,
    val longestReflectionTitle: String
)

class GetWritingInsightsUseCase {
    operator fun invoke(entries: List<JournalEntryEntity>): JournalInsights {
        if (entries.isEmpty()) {
            return JournalInsights(
                totalWords = 0,
                averageEntryLength = 0,
                writingFrequency = 0,
                favoriteWritingTime = "None",
                mostCommonMood = "None",
                longestReflectionWords = 0,
                longestReflectionTitle = ""
            )
        }

        var totalWords = 0
        var longestWords = 0
        var longestTitle = ""
        val moodCounts = mutableMapOf<String, Int>()
        val timeOfDayCounts = mutableMapOf<String, Int>()

        entries.forEach { entry ->
            val contentWords = entry.content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            totalWords += contentWords
            if (contentWords > longestWords) {
                longestWords = contentWords
                longestTitle = entry.title.ifBlank { "Untitled Reflection" }
            }

            if (entry.mood.isNotBlank()) {
                moodCounts[entry.mood] = moodCounts.getOrDefault(entry.mood, 0) + 1
            }

            val calendar = Calendar.getInstance().apply { timeInMillis = entry.timestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val timeOfDay = when (hour) {
                in 5..11 -> "Morning"
                in 12..16 -> "Afternoon"
                in 17..21 -> "Evening"
                else -> "Night"
            }
            timeOfDayCounts[timeOfDay] = timeOfDayCounts.getOrDefault(timeOfDay, 0) + 1
        }

        val avgLength = totalWords / entries.size
        val favoriteTime = timeOfDayCounts.maxByOrNull { it.value }?.key ?: "None"
        val commonMood = moodCounts.maxByOrNull { it.value }?.key ?: "None"

        return JournalInsights(
            totalWords = totalWords,
            averageEntryLength = avgLength,
            writingFrequency = entries.size,
            favoriteWritingTime = favoriteTime,
            mostCommonMood = commonMood,
            longestReflectionWords = longestWords,
            longestReflectionTitle = longestTitle
        )
    }
}
