package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusSessionEntity

class SaveFocusSessionUseCase(private val repository: FocusRepository) {
    suspend operator fun invoke(durationMinutes: Int, spaceName: String, notes: String, isFlowMode: Boolean, targetMinutes: Int) {
        val session = FocusSessionEntity(
            timestamp = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            spaceName = spaceName,
            notes = notes,
            isFlowMode = isFlowMode,
            targetMinutes = targetMinutes
        )
        repository.insertSession(session)
    }
}
