package com.example.ataraxia.features.focus.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun StopwatchAnalogClock(
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    val secondsHandAngle = (elapsedSeconds % 60) * 6f
    val minutes = elapsedSeconds / 60f
    val minutesHandAngle = (minutes % 30) * 12f

    val primaryColor = MaterialTheme.colorScheme.primary
    val textPrimary = DesignTokens.TextPrimary

    Canvas(
        modifier = modifier
    ) {
        val center = center
        val radius = size.minDimension / 2f

        drawCircle(
            color = textPrimary.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        for (i in 0 until 60) {
            val angle = i * 6f
            val isMajor = i % 5 == 0
            val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
            val tickWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
            val tickColor = if (isMajor) textPrimary.copy(alpha = 0.5f) else textPrimary.copy(alpha = 0.25f)

            val angleRad = Math.toRadians(angle.toDouble())
            val startX = (center.x + (radius - tickLength) * Math.sin(angleRad)).toFloat()
            val startY = (center.y - (radius - tickLength) * Math.cos(angleRad)).toFloat()
            val endX = (center.x + radius * Math.sin(angleRad)).toFloat()
            val endY = (center.y - radius * Math.cos(angleRad)).toFloat()

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = tickWidth
            )
        }

        val subDialCenter = Offset(center.x, center.y + radius * 0.3f)
        val subDialRadius = radius * 0.28f
        drawCircle(
            color = textPrimary.copy(alpha = 0.1f),
            radius = subDialRadius,
            center = subDialCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )
        for (i in 0 until 6) {
            val angle = i * 60f
            val angleRad = Math.toRadians(angle.toDouble())
            val tickLength = 3.dp.toPx()
            val startX = (subDialCenter.x + (subDialRadius - tickLength) * Math.sin(angleRad)).toFloat()
            val startY = (subDialCenter.y - (subDialRadius - tickLength) * Math.cos(angleRad)).toFloat()
            val endX = (subDialCenter.x + subDialRadius * Math.sin(angleRad)).toFloat()
            val endY = (subDialCenter.y - subDialRadius * Math.cos(angleRad)).toFloat()

            drawLine(
                color = textPrimary.copy(alpha = 0.3f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        val minAngleRad = Math.toRadians(minutesHandAngle.toDouble())
        val minHandLen = subDialRadius * 0.75f
        val minEndX = (subDialCenter.x + minHandLen * Math.sin(minAngleRad)).toFloat()
        val minEndY = (subDialCenter.y - minHandLen * Math.cos(minAngleRad)).toFloat()
        drawLine(
            color = primaryColor.copy(alpha = 0.8f),
            start = subDialCenter,
            end = Offset(minEndX, minEndY),
            strokeWidth = 2.dp.toPx()
        )
        drawCircle(
            color = primaryColor,
            radius = 2.5.dp.toPx(),
            center = subDialCenter
        )

        val secAngleRad = Math.toRadians(secondsHandAngle.toDouble())
        val secHandLen = radius * 0.82f
        val secEndX = (center.x + secHandLen * Math.sin(secAngleRad)).toFloat()
        val secEndY = (center.y - secHandLen * Math.cos(secAngleRad)).toFloat()
        drawLine(
            color = primaryColor, start = center, end = Offset(secEndX, secEndY), strokeWidth = 2.dp.toPx()
        )

        drawCircle(
            color = primaryColor,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}
