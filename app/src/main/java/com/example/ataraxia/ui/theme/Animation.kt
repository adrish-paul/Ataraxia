package com.example.ataraxia.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AtaraxiaAnimation(
    val Fast: Int = 150,
    val Normal: Int = 300,
    val Slow: Int = 400,
    val Breathing: Int = 5000,
    val EasingCurve: Easing = FastOutSlowInEasing
)

val LocalAnimation = staticCompositionLocalOf { AtaraxiaAnimation() }
