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
    val targetMinutes: Int = 0,
    
    // Expanded intention system
    val intentionName: String = "Other",
    val intentionIcon: String = "✨",
    val intentionColorHex: String = "#B9A7D6",
    
    // Completion status
    val completionStatus: String = "Completed", // "Completed" or "Cancelled"
    
    // Optional reflection inputs
    val reflectionEnjoyed: String = "",
    val reflectionDistracted: String = "",
    val reflectionFocusRate: Int = 0 // focus rating, e.g., 0 (none) or 1..5
)
