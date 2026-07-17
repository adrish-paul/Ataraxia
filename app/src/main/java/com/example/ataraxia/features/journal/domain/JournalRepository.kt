package com.example.ataraxia.features.journal.domain

import com.example.ataraxia.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getAllEntries(): Flow<List<JournalEntryEntity>>
    suspend fun insertEntry(entry: JournalEntryEntity)
    suspend fun deleteEntry(id: Long)
    suspend fun toggleFavorite(id: Long)
}
