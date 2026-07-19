package com.example.ataraxia.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.ataraxia.ui.theme.AtaraxiaTheme

@Composable
fun AtaraxiaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale down button slightly on press (150ms)
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = AtaraxiaTheme.animation.Fast,
            easing = AtaraxiaTheme.animation.EasingCurve
        ),
        label = "PrimaryButtonScale"
    )

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.secondary, // DustyLavender
            MaterialTheme.colorScheme.primary    // MoonlightIndigo
        )
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .then(
                if (enabled) {
                    Modifier.background(brush = gradientBrush, shape = MaterialTheme.shapes.large)
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = MaterialTheme.shapes.large)
                }
            ),
        enabled = enabled,
        shape = MaterialTheme.shapes.large, // 28dp radius
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Managed via background modifier
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Color.Transparent, // Managed via background modifier
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            ),
            modifier = Modifier.padding(vertical = AtaraxiaTheme.spacing.Space8),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AtaraxiaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = AtaraxiaTheme.animation.Fast,
            easing = AtaraxiaTheme.animation.EasingCurve
        ),
        label = "SecondaryButtonScale"
    )

    val strokeColor = if (isDestructive) {
        MaterialTheme.colorScheme.error // MutedRose
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = enabled,
        shape = MaterialTheme.shapes.large, // 28dp radius
        border = BorderStroke(1.dp, strokeColor.copy(alpha = 0.8f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor = strokeColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            ),
            modifier = Modifier.padding(vertical = AtaraxiaTheme.spacing.Space8),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AtaraxiaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = AtaraxiaTheme.animation.Fast,
            easing = AtaraxiaTheme.animation.EasingCurve
        ),
        label = "TextButtonScale"
    )

    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = textColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        )
    }
}
