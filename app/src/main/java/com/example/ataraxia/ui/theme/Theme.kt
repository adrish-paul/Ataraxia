package com.example.ataraxia.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import androidx.compose.runtime.staticCompositionLocalOf

val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }
val LocalAtaraxiaThemeMode = staticCompositionLocalOf { AtaraxiaThemeMode.LIGHT }

enum class AtaraxiaThemeMode {
    LIGHT, AURORA, COSMOS, FOREST, SAKURA, AQUA
}

private val LightColorScheme = lightColorScheme(
    primary = MoonlightIndigo,
    onPrimary = WarmIvory,
    secondary = DustyLavender,
    onSecondary = MoonlightIndigo,
    background = Color.Transparent,
    onBackground = MoonlightIndigo,
    surface = SoftCream,
    onSurface = MoonlightIndigo,
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = MoonlightIndigo,
    error = MutedRose,
    onError = WarmIvory
)

private val AuroraColorScheme = darkColorScheme(
    primary = DustyLavender,
    onPrimary = DeepIndigo,
    secondary = MoonlightIndigo,
    onSecondary = DustyLavender,
    background = Color.Transparent,
    onBackground = WarmIvory,
    surface = MidnightViolet,
    onSurface = WarmIvory,
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = WarmIvory,
    error = MutedRose,
    onError = DeepIndigo
)

private val CosmosColorScheme = darkColorScheme(
    primary = DustyLavender,
    onPrimary = NearBlack,
    secondary = MoonlightIndigo,
    onSecondary = DustyLavender,
    background = Color.Transparent,
    onBackground = WarmIvory,
    surface = MidnightViolet,
    onSurface = WarmIvory,
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = WarmIvory,
    error = MutedRose,
    onError = NearBlack
)

private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF8EB897),       // Sage green
    onPrimary = Color(0xFF0F1412),     // Dark forest background
    secondary = Color(0xFFC0DCC6),     // Lighter mint/sage green
    onSecondary = Color(0xFF8EB897),
    background = Color.Transparent,    // Make transparent
    onBackground = Color(0xFFE8F2EA),  // Very soft greenish white
    surface = Color(0xFF1B221E),       // Forest container
    onSurface = Color(0xFFE8F2EA),
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = Color(0xFFE8F2EA),
    error = MutedRose,
    onError = Color(0xFF0F1412)
)

private val SakuraColorScheme = lightColorScheme(
    primary = Color(0xFFE08EAD),       // Sakura pink
    onPrimary = Color(0xFFFCF5F7),     // Sakura background
    secondary = Color(0xFFF0B3CA),     // Rose pink
    onSecondary = Color(0xFFE08EAD),
    background = Color.Transparent,    // Make transparent
    onBackground = Color(0xFF5C2C3D),  // Dark berry text
    surface = Color(0xFFF9EAEF),       // Light pink container
    onSurface = Color(0xFF5C2C3D),
    surfaceVariant = Color.Transparent, // Make transparent
    onSurfaceVariant = Color(0xFF5C2C3D),
    error = MutedRose,
    onError = Color(0xFFFCF5F7)
)

private val AquaColorScheme = lightColorScheme(
    primary = Color(0xFF5B9BD5),       // Sea blue
    onPrimary = Color(0xFFF2F7FA),     // Ocean background
    secondary = Color(0xFFA5C8E1),     // Sky blue secondary
    onSecondary = Color(0xFF5B9BD5),
    background = Color.Transparent,    // Make transparent
    onBackground = Color(0xFF1A3E5C),  // Deep blue text
    surface = Color(0xFFE3EDF5),       // Sea container
    onSurface = Color(0xFF1A3E5C),
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = Color(0xFF1A3E5C),
    error = MutedRose,
    onError = Color(0xFFF2F7FA)
)

@Composable
fun AtaraxiaTheme(
    themeMode: AtaraxiaThemeMode = if (isSystemInDarkTheme()) AtaraxiaThemeMode.FOREST else AtaraxiaThemeMode.SAKURA,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AtaraxiaThemeMode.LIGHT -> {
            val context = LocalContext.current
            if (dynamicColor) {
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
        }
        AtaraxiaThemeMode.AURORA -> {
            val context = LocalContext.current
            if (dynamicColor) {
                dynamicDarkColorScheme(context)
            } else {
                AuroraColorScheme
            }
        }
        AtaraxiaThemeMode.COSMOS -> CosmosColorScheme
        AtaraxiaThemeMode.FOREST -> ForestColorScheme
        AtaraxiaThemeMode.SAKURA -> SakuraColorScheme
        AtaraxiaThemeMode.AQUA -> AquaColorScheme
    }

    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val isDark = themeMode in listOf(AtaraxiaThemeMode.AURORA, AtaraxiaThemeMode.COSMOS, AtaraxiaThemeMode.FOREST)
            androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val spacing = Spacing()
    val elevation = Elevation()
    val animation = AtaraxiaAnimation()

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalElevation provides elevation,
        LocalAnimation provides animation,
        LocalAtaraxiaThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AtaraxiaTypography,
            shapes = AtaraxiaShapes,
            content = content
        )
    }
}