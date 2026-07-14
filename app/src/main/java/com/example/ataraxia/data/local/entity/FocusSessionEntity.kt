package com.example.ataraxia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val durationMinutes: Int,
    val spaceName: String,
    val notes: String = "",
    val isFlowMode: Boolean = false,
    val targetMinutes: Int = 0
)
