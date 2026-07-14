package com.example.ataraxia.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun FloatingBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val items = listOf(
        NavigationItem("home", "Home", Icons.Outlined.Home),
        NavigationItem("journal", "Journal", Icons.Outlined.Book),
        NavigationItem("breathe", "Breathe", Icons.Outlined.LocalFlorist),
        NavigationItem("focus", "Focus", Icons.Outlined.Timer),
        NavigationItem("me", "Me", Icons.Outlined.Person)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
            .navigationBarsPadding()
            .padding(bottom = AtaraxiaTheme.spacing.Space16)
            .height(72.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = DesignTokens.CardBackground.copy(alpha = 0.9f),
        shadowElevation = AtaraxiaTheme.elevation.Floating
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AtaraxiaTheme.spacing.Space8),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = tween(
                        durationMillis = AtaraxiaTheme.animation.Fast,
                        easing = AtaraxiaTheme.animation.EasingCurve
                    ),
                    label = "NavTabScale"
                )

                val tintColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    DesignTokens.TextSecondary
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate(item.route)
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )

                    AnimatedVisibility(visible = isSelected) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = tintColor,
                            modifier = Modifier.padding(top = AtaraxiaTheme.spacing.Space4)
                        )
                    }
                }
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
