package com.example.ataraxia

import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.features.journal.domain.GetWritingInsightsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GetWritingInsightsUseCaseTest {

    @Test
    fun `invoke with empty list returns zero insights`() {
        val useCase = GetWritingInsightsUseCase()
        val result = useCase(emptyList())

        assertEquals(0, result.totalWords)
        assertEquals(0, result.averageEntryLength)
        assertEquals(0, result.writingFrequency)
        assertEquals("None", result.favoriteWritingTime)
        assertEquals("None", result.mostCommonMood)
        assertEquals(0, result.longestReflectionWords)
        assertEquals("", result.longestReflectionTitle)
    }

    @Test
    fun `invoke computes correct statistics`() {
        val useCase = GetWritingInsightsUseCase()

        // Create calendars representing morning and evening
        val morningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
        }
        val eveningCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
        }

        val entries = listOf(
            JournalEntryEntity(
                id = 1,
                title = "Morning Joy",
                content = "This is a lovely morning post.", // 6 words
                timestamp = morningCal.timeInMillis,
                mood = "☀️",
                weatherContext = ""
            ),
            JournalEntryEntity(
                id = 2,
                title = "Evening Reflection",
                content = "I am reflective. Calm waves.", // 5 words
                timestamp = eveningCal.timeInMillis,
                mood = "🍃",
                weatherContext = ""
            ),
            JournalEntryEntity(
                id = 3,
                title = "Morning Work",
                content = "Focusing on coding this beautiful offline project today.", // 9 words
                timestamp = morningCal.timeInMillis,
                mood = "☀️",
                weatherContext = ""
            )
        )

        val result = useCase(entries)

        // total: 6 + 5 + 8 = 19 words
        assertEquals(19, result.totalWords)
        // average: 19 / 3 = 6 words
        assertEquals(6, result.averageEntryLength)
        // count: 3
        assertEquals(3, result.writingFrequency)
        // common mood: ☀️ (appeared twice, 🍃 once)
        assertEquals("☀️", result.mostCommonMood)
        // favorite time: Morning (2 morning entries vs 1 evening entry)
        assertEquals("Morning", result.favoriteWritingTime)
        // longest entry: Morning Work (8 words)
        assertEquals(8, result.longestReflectionWords)
        assertEquals("Morning Work", result.longestReflectionTitle)
    }
}
