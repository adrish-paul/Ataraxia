package com.example.ataraxia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.ui.theme.LocalHazeState
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ScreenEnclosure(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = LocalHazeState.current
    val useHaze = hazeState != null
    val finalContainerColor = if (useHaze) {
        DesignTokens.CardBackground.copy(alpha = 0.45f)
    } else {
        DesignTokens.CardBackground
    }

    val boxModifier = modifier
        .clip(RoundedCornerShape(24.dp))
        .background(finalContainerColor)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 30.dp
                    noiseFactor = 0.02f
                    inputScale = HazeInputScale.Auto
                    alpha = 0.95f
                }
            } else {
                Modifier
            }
        )
        .border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
            shape = RoundedCornerShape(24.dp)
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = boxModifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            content = content
        )
    }
}
