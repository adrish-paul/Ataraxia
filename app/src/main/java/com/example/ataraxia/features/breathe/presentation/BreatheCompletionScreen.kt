package com.example.ataraxia.features.breathe.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun BreatheCompletionScreen(
    durationSeconds: Int,
    methodName: String,
    onContinue: (String) -> Unit
) {
    var selectedMood by remember { mutableStateOf("") }

    val bloomTransition = rememberInfiniteTransition(label = "BloomTransition")
    val scale by bloomTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BloomScale"
    )
    val alpha by bloomTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BloomAlpha"
    )

    val durationText = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        when {
            mins > 0 && secs > 0 -> "$mins min $secs sec"
            mins > 0 -> "$mins min"
            else -> "$secs sec"
        }
    }

    val encouragingMessage = remember(durationSeconds) {
        val messages = listOf(
            "You took time for yourself today.",
            "Every calm breath matters.",
            "A moment of stillness brings clarity.",
            "You have centered your mind."
        )
        messages[durationSeconds % messages.size]
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(AtaraxiaTheme.spacing.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

        // Center Content: Bloom Animation + Mindful Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    // Draw outer glowing soft bloom rings
                    drawCircle(
                        color = primaryColor.copy(alpha = alpha * 0.15f),
                        radius = radius * scale
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = alpha * 0.35f),
                        radius = radius * 0.75f * scale,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Core bloom circle
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.8f),
                        radius = radius * 0.45f
                    )
                }
                Text(
                    text = "🌸",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

            Text(
                text = "Session Completed",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))

            Text(
                text = encouragingMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

            Text(
                text = "You practiced $methodName for $durationText",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Content: Integrated Mood Check-in + Continue button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AtaraxiaTheme.spacing.Space16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space24)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
            ) {
                Text(
                    text = "How do you feel now?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = DesignTokens.TextSecondary,
                    textAlign = TextAlign.Center
                )

                val moods = listOf("😌 Calm", "😴 Sleepy", "😊 Happy", "😤 Relieved", "🤔 Neutral")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.take(3).forEach { mood ->
                        MoodChip(
                            mood = mood,
                            isSelected = selectedMood == mood,
                            onClick = { selectedMood = if (selectedMood == mood) "" else mood },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.drop(3).forEach { mood ->
                        MoodChip(
                            mood = mood,
                            isSelected = selectedMood == mood,
                            onClick = { selectedMood = if (selectedMood == mood) "" else mood },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Box(modifier = Modifier.weight(1f))
                }
            }

            AtaraxiaPrimaryButton(
                text = "Continue",
                onClick = { onContinue(selectedMood) }
            )
        }
    }
}

@Composable
private fun MoodChip(
    mood: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else DesignTokens.CardBackground
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = mood.take(2), fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mood.drop(2).trim(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
            )
        }
    }
}
