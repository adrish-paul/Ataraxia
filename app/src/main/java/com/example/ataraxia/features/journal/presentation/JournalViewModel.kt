package com.example.ataraxia.features.journal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.data.preferences.JournalDraft
import com.example.ataraxia.data.preferences.SanctuaryPreferences
import com.example.ataraxia.features.journal.domain.DeleteJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.GetJournalEntriesUseCase
import com.example.ataraxia.features.journal.domain.GetWritingInsightsUseCase
import com.example.ataraxia.features.journal.domain.JournalInsights
import com.example.ataraxia.features.journal.domain.SaveJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    getEntriesUseCase: GetJournalEntriesUseCase,
    private val saveEntryUseCase: SaveJournalEntryUseCase,
    private val deleteEntryUseCase: DeleteJournalEntryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val preferences: SanctuaryPreferences
) : ViewModel() {

    val allEntries: StateFlow<List<JournalEntryEntity>> = getEntriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Writing insights flow calculated offline locally
    private val getWritingInsightsUseCase = GetWritingInsightsUseCase()
    val insightsFlow: StateFlow<JournalInsights> = allEntries
        .map { getWritingInsightsUseCase(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            getWritingInsightsUseCase(emptyList())
        )

    // Draft management
    val journalDraft: StateFlow<JournalDraft?> = preferences.journalDraftFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveDraft(title: String, content: String, mood: String, tags: String, prompt: String, timestamp: Long) {
        viewModelScope.launch {
            preferences.saveJournalDraft(title, content, mood, tags, prompt, timestamp)
        }
    }

    fun clearDraft() {
        viewModelScope.launch {
            preferences.clearJournalDraft()
        }
    }

    fun addEntry(
        title: String,
        content: String,
        mood: String,
        weatherContext: String,
        isFavorite: Boolean = false,
        tags: String = "",
        imagePath: String = "",
        voicePath: String = ""
    ) {
        viewModelScope.launch {
            saveEntryUseCase(title, content, mood, weatherContext, isFavorite, tags, imagePath, voicePath)
            // Clear draft upon successful saving
            preferences.clearJournalDraft()
        }
    }

    fun toggleFavorite(entry: JournalEntryEntity) {
        viewModelScope.launch {
            toggleFavoriteUseCase(entry.id)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            deleteEntryUseCase(id)
        }
    }
}
