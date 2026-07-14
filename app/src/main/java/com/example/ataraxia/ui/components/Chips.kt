package com.example.ataraxia.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun MoodChip(
    moodEmoji: String,
    moodLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = AtaraxiaTheme.animation.Fast),
        label = "MoodChipScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f) // Glowing soft lavender
        } else {
            DesignTokens.CardBackground
        },
        label = "MoodChipContainerColor"
    )

    val borderStroke = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }),
        shape = MaterialTheme.shapes.small, // 12dp radius
        color = containerColor,
        border = borderStroke,
        tonalElevation = if (isSelected) AtaraxiaTheme.elevation.Low else AtaraxiaTheme.elevation.None
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(
                    horizontal = AtaraxiaTheme.spacing.Space16,
                    vertical = AtaraxiaTheme.spacing.Space8
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = moodEmoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
            Text(
                text = moodLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (isSelected) DesignTokens.TextPrimary else DesignTokens.TextSecondary
            )
        }
    }
}

@Composable
fun DurationChip(
    durationText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = AtaraxiaTheme.animation.Fast),
        label = "DurationChipScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
        } else {
            DesignTokens.CardBackground
        },
        label = "DurationChipContainerColor"
    )

    val borderStroke = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }),
        shape = MaterialTheme.shapes.medium, // 24dp radius or Pill
        color = containerColor,
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(
                    horizontal = AtaraxiaTheme.spacing.Space20,
                    vertical = AtaraxiaTheme.spacing.Space8
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = durationText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (isSelected) DesignTokens.TextPrimary else DesignTokens.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
