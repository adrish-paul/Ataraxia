package com.example.ataraxia.features.journal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.features.journal.domain.DeleteJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.GetJournalEntriesUseCase
import com.example.ataraxia.features.journal.domain.SaveJournalEntryUseCase
import com.example.ataraxia.features.journal.domain.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    getEntriesUseCase: GetJournalEntriesUseCase,
    private val saveEntryUseCase: SaveJournalEntryUseCase,
    private val deleteEntryUseCase: DeleteJournalEntryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val allEntries: StateFlow<List<JournalEntryEntity>> = getEntriesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
