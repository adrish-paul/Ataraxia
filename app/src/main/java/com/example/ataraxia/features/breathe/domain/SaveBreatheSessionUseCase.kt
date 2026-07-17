package com.example.ataraxia.features.breathe.domain

import com.example.ataraxia.data.local.entity.BreatheSessionEntity

class SaveBreatheSessionUseCase(private val repository: BreatheRepository) {
    suspend operator fun invoke(durationSeconds: Int, method: String, mood: String) {
        val session = BreatheSessionEntity(
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            method = method,
            mood = mood
        )
        repository.insertSession(session)
    }
}
