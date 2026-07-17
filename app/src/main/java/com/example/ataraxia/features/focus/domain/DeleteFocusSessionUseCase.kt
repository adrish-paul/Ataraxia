package com.example.ataraxia.features.focus.domain

class DeleteFocusSessionUseCase(private val repository: FocusRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteSession(id)
    }
}
