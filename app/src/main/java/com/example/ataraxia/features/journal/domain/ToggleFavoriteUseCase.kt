package com.example.ataraxia.features.journal.domain

class ToggleFavoriteUseCase(private val repository: JournalRepository) {
    suspend operator fun invoke(id: Long) {
        repository.toggleFavorite(id)
    }
}
