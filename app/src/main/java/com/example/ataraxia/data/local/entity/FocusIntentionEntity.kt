package com.example.ataraxia.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_intentions")
data class FocusIntentionEntity(
    @PrimaryKey val name: String,
    val icon: String,
    val colorHex: String,
    val description: String,
    val isCustom: Boolean = true
)
