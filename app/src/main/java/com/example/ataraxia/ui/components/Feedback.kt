package com.example.ataraxia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun AtaraxiaSnackbar(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // 24dp radius
        color = DesignTokens.CardBackground,
        tonalElevation = AtaraxiaTheme.elevation.Floating,
        shadowElevation = AtaraxiaTheme.elevation.Low
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AtaraxiaTheme.spacing.Space20,
                vertical = AtaraxiaTheme.spacing.Space16
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.TextPrimary
            )
        }
    }
}

@Composable
fun EmptyState(
    illustration: @Composable BoxScope.() -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AtaraxiaTheme.spacing.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center,
            content = illustration
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space20))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DesignTokens.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = DesignTokens.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
            AtaraxiaPrimaryButton(
                text = actionLabel,
                onClick = onActionClick,
                modifier = Modifier.width(220.dp) // Centered compact primary action
            )
        }
    }
}
