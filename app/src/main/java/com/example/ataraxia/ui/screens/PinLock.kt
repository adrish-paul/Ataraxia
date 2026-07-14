package com.example.ataraxia.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlockSuccess: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .statusBarsPadding()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = AtaraxiaTheme.spacing.Space48)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Lock Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
            
            Text(
                text = "Secure Sanctuary",
                style = MaterialTheme.typography.headlineLarge,
                color = DesignTokens.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
            
            Text(
                text = if (errorMessage.isEmpty()) "Enter your 4-digit PIN to unlock" else errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = if (errorMessage.isEmpty()) DesignTokens.TextSecondary else MaterialTheme.colorScheme.error
            )
        }

        // Indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < enteredPin.length
                val color by animateColorAsState(
                    targetValue = if (isFilled) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary.copy(alpha = 0.3f),
                    label = "IndicatorColor"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Numerical Keypad (Minimalist circular buttons)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AtaraxiaTheme.spacing.Space48),
            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "delete")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .then(
                                    if (cell.isNotEmpty()) {
                                        Modifier.clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            errorMessage = ""
                                            if (cell == "delete") {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            } else {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += cell
                                                    if (enteredPin.length == 4) {
                                                        if (enteredPin == correctPin) {
                                                            onUnlockSuccess()
                                                        } else {
                                                            errorMessage = "Incorrect PIN. Try again."
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cell.isNotEmpty()) {
                                if (cell == "delete") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                        contentDescription = "Delete",
                                        tint = DesignTokens.TextPrimary
                                    )
                                } else {
                                    Text(
                                        text = cell,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = DesignTokens.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
