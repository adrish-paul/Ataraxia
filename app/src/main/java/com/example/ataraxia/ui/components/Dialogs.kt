package com.example.ataraxia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                
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
                    Box(modifier = Modifier.weight(1f)) {
                        AtaraxiaSecondaryButton(
                            text = dismissLabel,
                            onClick = onDismiss
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
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
