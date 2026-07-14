package com.example.ataraxia.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Elevation(
    val None: Dp = 0.dp,
    val Low: Dp = 2.dp,
    val Medium: Dp = 4.dp,
    val Floating: Dp = 8.dp
)

val LocalElevation = staticCompositionLocalOf { Elevation() }
