package com.example.ataraxia.features.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.features.focus.domain.DeleteFocusSessionUseCase
import com.example.ataraxia.features.focus.domain.GetFocusSessionsUseCase
import com.example.ataraxia.features.focus.domain.SaveFocusSessionUseCase
import com.example.ataraxia.features.focus.domain.GetFocusIntentionsUseCase
import com.example.ataraxia.features.focus.domain.SaveFocusIntentionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusViewModel(
    getSessionsUseCase: GetFocusSessionsUseCase,
    private val saveSessionUseCase: SaveFocusSessionUseCase,
    private val deleteSessionUseCase: DeleteFocusSessionUseCase,
    getIntentionsUseCase: GetFocusIntentionsUseCase,
    private val saveIntentionUseCase: SaveFocusIntentionUseCase
) : ViewModel() {

    val allSessions: StateFlow<List<FocusSessionEntity>> = getSessionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationMinutes: StateFlow<Int> = getSessionsUseCase.getTotalDurationMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allIntentions: StateFlow<List<FocusIntentionEntity>> = getIntentionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logSession(
        durationMinutes: Int,
        spaceName: String,
        notes: String = "",
        isFlowMode: Boolean = false,
        targetMinutes: Int = 0,
        intentionName: String = "Other",
        intentionIcon: String = "✨",
        intentionColorHex: String = "#B9A7D6",
        completionStatus: String = "Completed",
        reflectionEnjoyed: String = "",
        reflectionDistracted: String = "",
        reflectionFocusRate: Int = 0
    ) {
        viewModelScope.launch {
            saveSessionUseCase(
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
        }
    }

    fun addCustomIntention(name: String, icon: String, colorHex: String, description: String) {
        viewModelScope.launch {
            saveIntentionUseCase(name, icon, colorHex, description)
        }
    }

    fun deleteFocusSession(id: Long) {
        viewModelScope.launch {
            deleteSessionUseCase(id)
        }
    }
}
