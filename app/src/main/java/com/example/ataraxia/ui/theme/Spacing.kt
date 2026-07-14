package com.example.ataraxia.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class Spacing(
    val default: Dp = 0.dp,
    val Space4: Dp = 4.dp,
    val Space8: Dp = 8.dp,
    val Space12: Dp = 12.dp,
    val Space16: Dp = 16.dp,
    val Space20: Dp = 20.dp,
    val Space24: Dp = 24.dp,
    val Space32: Dp = 32.dp,
    val Space48: Dp = 48.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
