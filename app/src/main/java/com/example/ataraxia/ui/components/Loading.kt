package com.example.ataraxia.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun GentleLoading(
    modifier: Modifier = Modifier,
    message: String = "Preparing your quiet space..."
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Three dots animating in a breathing pattern with offset delays
            (0..2).forEach { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "BreathingDotTransition_$index")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            delayMillis = index * 200,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "BreathingDotAlpha_$index"
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = DesignTokens.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
