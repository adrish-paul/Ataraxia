package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusIntentionEntity

class SaveFocusIntentionUseCase(private val repository: FocusRepository) {
    suspend operator fun invoke(name: String, icon: String, colorHex: String, description: String) {
        val intention = FocusIntentionEntity(
            name = name,
            icon = icon,
            colorHex = colorHex,
            description = description,
            isCustom = true
        )
        repository.insertIntention(intention)
    }
}
