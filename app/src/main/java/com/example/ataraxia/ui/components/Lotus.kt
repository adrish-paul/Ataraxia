package com.example.ataraxia.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.AtaraxiaTheme

enum class BreatheState {
    INHALE, HOLD, EXHALE, REST
}

@Composable
fun AnimatedLotus(
    breatheState: BreatheState,
    cycleDurationMs: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    enableGlow: Boolean = true
) {
    // ── Infinite ambient animations & LFO ──────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "LotusAmbient")

    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LotusIdlePulse"
    )

    // Staggered per-tier opacity pulses for an organic, layered feel
    val alphaTier1 by infiniteTransition.animateFloat(
        initialValue = 0.48f, targetValue = 0.58f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "AlphaTier1"
    )
    val alphaTier2 by infiniteTransition.animateFloat(
        initialValue = 0.68f, targetValue = 0.80f,
        animationSpec = infiniteRepeatable(tween(1900, 300, FastOutSlowInEasing), RepeatMode.Reverse),
        label = "AlphaTier2"
    )
    val alphaTier3 by infiniteTransition.animateFloat(
        initialValue = 0.82f, targetValue = 0.92f,
        animationSpec = infiniteRepeatable(tween(2500, 600, FastOutSlowInEasing), RepeatMode.Reverse),
        label = "AlphaTier3"
    )
    // Pulsing glow radius
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "GlowPulse"
    )

    // ── State-driven targets with organic LFO scaling ─────────────────────
    val baseScale = when (breatheState) {
        BreatheState.INHALE -> 1.15f
        BreatheState.HOLD   -> 1.15f
        BreatheState.EXHALE -> 0.70f
        BreatheState.REST   -> 0.65f
    }
    val targetScale = when (breatheState) {
        BreatheState.HOLD -> baseScale * idlePulse
        BreatheState.REST -> baseScale * idlePulse
        else -> baseScale
    }

    val targetSpreadAngle = when (breatheState) {
        BreatheState.INHALE -> 42f
        BreatheState.HOLD   -> 44f
        BreatheState.EXHALE -> 14f
        BreatheState.REST   -> 10f
    }

    // Outer petals rotate gently on HOLD for a "held breath" feel
    val targetOuterRotation = when (breatheState) {
        BreatheState.HOLD   -> 5f
        BreatheState.REST   -> -2f
        else                -> 0f
    }

    val animDuration = cycleDurationMs.coerceAtLeast(600)
    val exhaleColor = lerp(color, color.copy(alpha = 0.55f), 0.4f)

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(animDuration, easing = AtaraxiaTheme.animation.EasingCurve),
        label = "LotusScale"
    )
    val spreadAngle by animateFloatAsState(
        targetValue = targetSpreadAngle,
        animationSpec = tween(animDuration, easing = AtaraxiaTheme.animation.EasingCurve),
        label = "LotusPetalSpread"
    )
    val outerRotation by animateFloatAsState(
        targetValue = targetOuterRotation,
        animationSpec = tween(animDuration, easing = AtaraxiaTheme.animation.EasingCurve),
        label = "LotusOuterRot"
    )
    // 0f = full color, 1f = exhale dimmed color
    val colorShift by animateFloatAsState(
        targetValue = if (breatheState == BreatheState.EXHALE || breatheState == BreatheState.REST) 1f else 0f,
        animationSpec = tween(animDuration),
        label = "LotusColorShift"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = when (breatheState) {
            BreatheState.HOLD   -> 0.35f
            BreatheState.REST   -> 0.28f
            else                -> 0.12f
        },
        animationSpec = tween(900),
        label = "LotusGlowAlpha"
    )

    val renderColor = lerp(color, exhaleColor, colorShift)
    val petalPath = remember { Path() }

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val petalWidth   = size.width  * 0.28f
            val petalHeight  = size.height * 0.58f
            val scaledWidth  = petalWidth  * scale
            val scaledHeight = petalHeight * scale

            // 1. Ambient glow ring
            if (enableGlow) {
                drawCircle(
                    color  = renderColor.copy(alpha = glowAlpha),
                    radius = (size.width / 2.3f) * scale * glowPulse,
                    center = center
                )
            }

            // 2. Outer tier — rotation applied for HOLD pulse
            rotate(outerRotation, pivot = center) {
                drawPetal(petalPath, center, scaledWidth, scaledHeight, -60f - (spreadAngle * 0.5f), renderColor.copy(alpha = alphaTier1))
                drawPetal(petalPath, center, scaledWidth, scaledHeight,  60f + (spreadAngle * 0.5f), renderColor.copy(alpha = alphaTier1))
            }

            // 3. Mid tier
            drawPetal(petalPath, center, scaledWidth, scaledHeight, -30f - (spreadAngle * 0.2f), renderColor.copy(alpha = alphaTier2))
            drawPetal(petalPath, center, scaledWidth, scaledHeight,  30f + (spreadAngle * 0.2f), renderColor.copy(alpha = alphaTier2))

            // 4. Inner spread
            drawPetal(petalPath, center, scaledWidth, scaledHeight, -spreadAngle, renderColor.copy(alpha = alphaTier3))
            drawPetal(petalPath, center, scaledWidth, scaledHeight,  spreadAngle, renderColor.copy(alpha = alphaTier3))

            // 5. Central petal — always full render color
            drawPetal(petalPath, center, scaledWidth, scaledHeight, 0f, renderColor)
        }
    }
}

private fun DrawScope.drawPetal(
    petalPath: Path,
    center: Offset,
    width: Float,
    height: Float,
    rotationAngle: Float,
    color: Color
) {
    rotate(rotationAngle, pivot = center) {
        petalPath.reset()
        val startX = center.x
        val startY = center.y + (height * 0.2f)
        val endX   = center.x
        val endY   = center.y - (height * 0.8f)
        petalPath.moveTo(startX, startY)
        petalPath.cubicTo(
            startX - width, startY - (height * 0.2f),
            startX - (width * 0.7f), endY + (height * 0.1f),
            endX, endY
        )
        petalPath.cubicTo(
            startX + (width * 0.7f), endY + (height * 0.1f),
            startX + width, startY - (height * 0.2f),
            startX, startY
        )
        petalPath.close()
        drawPath(path = petalPath, color = color)
    }
}
