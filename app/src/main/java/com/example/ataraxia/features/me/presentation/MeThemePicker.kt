package com.example.ataraxia.features.me.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DesignTokens

data class ThemeItem(val name: String, val desc: String, val badge: String)

@Composable
fun MeThemePicker(
    showThemeDialog: Boolean,
    currentThemeMode: AtaraxiaThemeMode,
    onDismiss: () -> Unit,
    onThemeChange: (AtaraxiaThemeMode) -> Unit
) {
    if (showThemeDialog) {
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Sanctuary Theme",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextPrimary
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = DesignTokens.TextSecondary
                            )
                        }
                    }

                    val themes = listOf(
                        ThemeItem("🌸 Sakura", "🌸 Pink accented light mode", "Pink"),
                        ThemeItem("🌿 Forest", "🌿 Green accented dark mode", "Green"),
                        ThemeItem("🌙 Cosmos", "🌙 Deep black AMOLED dark mode", "Amoled"),
                        ThemeItem("💮 Serene", "💮 Original calm light mode", "Serene"),
                        ThemeItem("✨ Aurora", "✨ Lavender accented dark mode", "Dark"),
                        ThemeItem("🌊 Aqua", "🌊 Blue ocean light mode", "Blue")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                    ) {
                        themes.forEach { themeItem ->
                            val themeModeVal = when (themeItem.name) {
                                "💮 Serene" -> AtaraxiaThemeMode.LIGHT
                                "✨ Aurora" -> AtaraxiaThemeMode.AURORA
                                "🌙 Cosmos" -> AtaraxiaThemeMode.COSMOS
                                "🌿 Forest" -> AtaraxiaThemeMode.FOREST
                                "🌸 Sakura" -> AtaraxiaThemeMode.SAKURA
                                "🌊 Aqua" -> AtaraxiaThemeMode.AQUA
                                else -> AtaraxiaThemeMode.LIGHT
                            }
                            val isSelected = currentThemeMode == themeModeVal
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        onThemeChange(themeModeVal)
                                    }
                                    .padding(AtaraxiaTheme.spacing.Space12)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Outlined.ColorLens else Icons.Outlined.Palette,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                        Column {
                                            Text(
                                                text = themeItem.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
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
    }
}
