package com.example.ataraxia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = AtaraxiaTheme.spacing.Space20,
                vertical = AtaraxiaTheme.spacing.Space16
            )
    } else {
        modifier
            .fillMaxWidth()
            .padding(
                horizontal = AtaraxiaTheme.spacing.Space20,
                vertical = AtaraxiaTheme.spacing.Space16
            )
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = DesignTokens.TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space4))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
            }
        }
        
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
            trailingContent()
        }
    }
}
