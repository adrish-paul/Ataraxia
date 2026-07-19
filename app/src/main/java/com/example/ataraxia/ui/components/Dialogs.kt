package com.example.ataraxia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun AtaraxiaDialog(
    title: String,
    description: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        val currentView = androidx.compose.ui.platform.LocalView.current
        var window: android.view.Window? = null
        var parentView = currentView.parent
        while (parentView != null) {
            if (parentView is androidx.compose.ui.window.DialogWindowProvider) {
                window = parentView.window
                break
            }
            parentView = parentView.parent
        }
        window?.let { w ->
            w.setBackgroundDrawableResource(android.R.color.transparent)
            w.decorView.setBackgroundResource(android.R.color.transparent)
            w.setElevation(0f)
            w.decorView.elevation = 0f
        }
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large, // 28dp radius
            color = DesignTokens.CardBackground,
            tonalElevation = AtaraxiaTheme.elevation.Medium
        ) {
            Column(
                modifier = Modifier.padding(AtaraxiaTheme.spacing.Space24)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = DesignTokens.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                HorizontalDivider(
                    color = DesignTokens.TextSecondary.copy(alpha = 0.12f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DesignTokens.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissLabel.isNotEmpty()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaSecondaryButton(
                                text = dismissLabel,
                                onClick = onDismiss
                            )
                        }
                    }
                    Box(modifier = if (dismissLabel.isNotEmpty()) Modifier.weight(1f) else Modifier.fillMaxWidth()) {
                        if (isDestructive) {
                            AtaraxiaSecondaryButton(
                                text = confirmLabel,
                                onClick = onConfirm,
                                isDestructive = true
                            )
                        } else {
                            AtaraxiaPrimaryButton(
                                text = confirmLabel,
                                onClick = onConfirm
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AtaraxiaAudioSelectorDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    selectedSound: String,
    onSoundSelected: (String) -> Unit,
    soundVolume: Float,
    onVolumeChanged: (Float) -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            val currentView = androidx.compose.ui.platform.LocalView.current
            var window: android.view.Window? = null
            var parentView = currentView.parent
            while (parentView != null) {
                if (parentView is androidx.compose.ui.window.DialogWindowProvider) {
                    window = parentView.window
                    break
                }
                parentView = parentView.parent
            }
            window?.let { w ->
                w.setBackgroundDrawableResource(android.R.color.transparent)
                w.decorView.setBackgroundResource(android.R.color.transparent)
                w.setElevation(0f)
                w.decorView.elevation = 0f
            }
            LunafloraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space8)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AtaraxiaTheme.spacing.Space8),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "🌿 Ambient Sound",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )

                    val ambientSounds = listOf("None", "Rain", "Forest", "Ocean", "Wind", "Fireplace", "Night")

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ambientSounds.forEach { sound ->
                            val isSelected = selectedSound == sound
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onSoundSelected(sound)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sound,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedSound != "None") {
                        HorizontalDivider(color = DesignTokens.TextSecondary.copy(alpha = 0.1f))
                        Text(
                            text = "Volume",
                            style = MaterialTheme.typography.labelSmall,
                            color = DesignTokens.TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = "Volume",
                                tint = DesignTokens.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = soundVolume,
                                onValueChange = onVolumeChanged,
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(soundVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextSecondary
                            )
                        }
                    }

                    AtaraxiaPrimaryButton(
                        text = "Done",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}
