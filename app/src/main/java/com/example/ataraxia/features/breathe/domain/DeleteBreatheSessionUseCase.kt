package com.example.ataraxia.features.breathe.domain

class DeleteBreatheSessionUseCase(private val repository: BreatheRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteSession(id)
    }
}
