package com.example.ataraxia.features.journal.domain

class DeleteJournalEntryUseCase(private val repository: JournalRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteEntry(id)
    }
}
