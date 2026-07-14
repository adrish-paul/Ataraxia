package com.example.ataraxia.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val animationTokens = AtaraxiaTheme.animation

    LaunchedEffect(key1 = true) {
        // Fade in logo slowly (1000ms)
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = animationTokens.EasingCurve
            )
        )
        // Linger visual focus
        delay(600)
        // Fade out logo slowly (600ms)
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 600,
                easing = animationTokens.EasingCurve
            )
        )
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = "Ataraxia Lotus Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(120.dp)
                .alpha(alphaAnim.value)
        )
    }
}
