package com.example.ataraxia.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.materials.HazeMaterials
import com.example.ataraxia.ui.theme.LocalHazeState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlin.OptIn

@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun LunafloraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = DesignTokens.CardBackground,
    border: BorderStroke? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    val useHaze = hazeState != null

    val finalContainerColor = if (useHaze) {
        containerColor.copy(alpha = 0.75f)
    } else {
        containerColor
    }

    val cardModifier = modifier
        .clip(MaterialTheme.shapes.large)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    val finalModifier = if (hazeState != null) {
        cardModifier.hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
            blurRadius = 30.dp
            noiseFactor = 0.02f
            inputScale = HazeInputScale.Auto
            alpha = 0.95f
        }
    } else {
        cardModifier
    }

    Card(
        modifier = finalModifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = finalContainerColor,
            contentColor = DesignTokens.TextPrimary
        ),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (useHaze) 0.dp else AtaraxiaTheme.elevation.Low
        )
    ) {
        Box(
            modifier = Modifier.padding(AtaraxiaTheme.spacing.Space20),
            content = content
        )
    }
}

@Composable
fun GreetingCard(
    greeting: String,
    date: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable BoxScope.() -> Unit)? = null
) {
    LunafloraCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = DesignTokens.TextPrimary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space4))
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
            }
            if (illustration != null) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                    content = illustration
                )
            }
        }
    }
}

@Composable
fun QuoteCard(
    quote: String,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    LunafloraCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☾ Quiet Affirmation",
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                if (onRefresh != null) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh Affirmation",
                            tint = DesignTokens.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.25
                ),
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = AtaraxiaTheme.spacing.Space12)
            )
        }
    }
}

@Composable
fun ReflectionCard(
    prompt: String,
    onBeginWriting: () -> Unit,
    modifier: Modifier = Modifier
) {
    LunafloraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Today's Reflection",
                style = MaterialTheme.typography.titleLarge,
                color = DesignTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.TextSecondary
            )
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space20))
            AtaraxiaPrimaryButton(
                text = "Begin Writing",
                onClick = onBeginWriting
            )
        }
    }
}

@Composable
fun JournalCard(
    title: String,
    snippet: String,
    date: String,
    mood: String,
    metadataContext: String, // e.g. "🌧️ Rainy Evening • 8:42 PM"
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LunafloraCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$mood  $date",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = metadataContext,
                    style = MaterialTheme.typography.labelLarge,
                    color = DesignTokens.TextSecondary
                )
            }
            if (title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = DesignTokens.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


