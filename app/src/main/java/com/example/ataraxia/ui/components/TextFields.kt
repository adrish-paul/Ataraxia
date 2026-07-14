package com.example.ataraxia.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun PrimaryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    maxLength: Int? = null
) {
    TextField(
        value = value,
        onValueChange = {
            if (maxLength == null || it.length <= maxLength) {
                onValueChange(it)
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.TextSecondary.copy(alpha = 0.7f)
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium, // 24dp radius
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DesignTokens.CardBackground,
            unfocusedContainerColor = DesignTokens.CardBackground,
            disabledContainerColor = DesignTokens.CardBackground.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = DesignTokens.TextPrimary,
            unfocusedTextColor = DesignTokens.TextPrimary,
            cursorColor = DesignTokens.PrimaryAccent
        )
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            ),
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
}
