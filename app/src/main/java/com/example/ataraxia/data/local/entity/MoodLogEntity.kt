package com.example.ataraxia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_logs")
data class MoodLogEntity(
    @PrimaryKey
    val dateStr: String, // format: "yyyyMMdd"
    val mood: String,
    val timestamp: Long
)
