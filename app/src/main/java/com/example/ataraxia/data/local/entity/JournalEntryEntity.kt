package com.example.ataraxia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val mood: String,
    val weatherContext: String,
    val isFavorite: Boolean = false,
    val tags: String = "",
    val imagePath: String = "",
    val voicePath: String = ""
)
