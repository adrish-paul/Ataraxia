package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusSessionEntity

class SaveFocusSessionUseCase(private val repository: FocusRepository) {
    suspend operator fun invoke(
        durationMinutes: Int,
        spaceName: String,
        notes: String,
        isFlowMode: Boolean,
        targetMinutes: Int,
        intentionName: String,
        intentionIcon: String,
        intentionColorHex: String,
        completionStatus: String,
        reflectionEnjoyed: String,
        reflectionDistracted: String,
        reflectionFocusRate: Int
    ) {
        val session = FocusSessionEntity(
            timestamp = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            spaceName = spaceName,
            notes = notes,
            isFlowMode = isFlowMode,
            targetMinutes = targetMinutes,
            intentionName = intentionName,
            intentionIcon = intentionIcon,
            intentionColorHex = intentionColorHex,
            completionStatus = completionStatus,
            reflectionEnjoyed = reflectionEnjoyed,
            reflectionDistracted = reflectionDistracted,
            reflectionFocusRate = reflectionFocusRate
        )
        repository.insertSession(session)
    }
}
