package com.example.ataraxia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We set default font family as system sans-serif which is rounded-friendly on modern Android
val LunafloraFontFamily = FontFamily.SansSerif

val AtaraxiaTypography = Typography(
    // H1: Large Headings
    displayLarge = TextStyle(
        fontFamily = LunafloraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    
    // H2: Sub-headings
    headlineLarge = TextStyle(
        fontFamily = LunafloraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    
    // Section Title
    titleLarge = TextStyle(
        fontFamily = LunafloraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    ),
    
    // Body (Default Text)
    bodyLarge = TextStyle(
        fontFamily = LunafloraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Caption / Detail Text
    labelLarge = TextStyle(
        fontFamily = LunafloraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    )
)