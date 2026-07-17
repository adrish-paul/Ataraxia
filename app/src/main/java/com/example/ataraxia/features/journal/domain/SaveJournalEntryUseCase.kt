package com.example.ataraxia.features.journal.domain

import com.example.ataraxia.data.local.entity.JournalEntryEntity

class SaveJournalEntryUseCase(private val repository: JournalRepository) {
    suspend operator fun invoke(
        title: String,
        content: String,
        mood: String,
        weatherContext: String,
        isFavorite: Boolean,
        tags: String,
        imagePath: String,
        voicePath: String
    ) {
        val entry = JournalEntryEntity(
            timestamp = System.currentTimeMillis(),
            title = title,
            content = content,
            mood = mood,
            weatherContext = weatherContext,
            isFavorite = isFavorite,
            tags = tags,
            imagePath = imagePath,
            voicePath = voicePath
        )
        repository.insertEntry(entry)
    }
}
