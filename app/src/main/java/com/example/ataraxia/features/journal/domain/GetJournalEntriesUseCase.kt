package com.example.ataraxia.features.journal.domain

import com.example.ataraxia.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

class GetJournalEntriesUseCase(private val repository: JournalRepository) {
    operator fun invoke(): Flow<List<JournalEntryEntity>> = repository.getAllEntries()
}
