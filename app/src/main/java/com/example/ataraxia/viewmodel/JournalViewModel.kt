package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.JournalEntryEntity
import com.example.ataraxia.data.repository.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(private val repository: JournalRepository) : ViewModel() {

    val allEntries: StateFlow<List<JournalEntryEntity>> = repository.allEntries
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
            val entry = JournalEntryEntity(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                mood = mood,
                weatherContext = weatherContext,
                isFavorite = isFavorite,
                tags = tags,
                imagePath = imagePath,
                voicePath = voicePath
            )
            repository.insert(entry)
        }
    }

    fun toggleFavorite(entry: JournalEntryEntity) {
        viewModelScope.launch {
            val updated = entry.copy(isFavorite = !entry.isFavorite)
            repository.update(updated)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
