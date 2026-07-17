package com.example.ataraxia.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.GreetingCard
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.MoodChip
import com.example.ataraxia.ui.components.QuoteCard
import com.example.ataraxia.ui.components.SectionHeader
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun HomeScreen(
    name: String,
    isFirstLaunch: Boolean,
    currentThemeMode: AtaraxiaThemeMode,
    onFirstLaunchCompleted: () -> Unit,
    onQuickBreathe: () -> Unit,
    onQuickJournal: () -> Unit,
    onQuickFocus: () -> Unit,
    scrollToTopKey: Int = 0
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0) scrollState.animateScrollTo(0)
    }
    var selectedMood by remember { mutableStateOf("") }
    var currentQuote by remember { mutableStateOf("One gentle breath can change everything.") }

    LaunchedEffect(Unit) {
        if (isFirstLaunch) {
            onFirstLaunchCompleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
            .statusBarsPadding()
            .padding(horizontal = AtaraxiaTheme.spacing.Space24)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AtaraxiaTheme.spacing.Space16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = "Ataraxia Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = "Ataraxia",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        val isFirstSession = remember { isFirstLaunch }
        val greetingPrefix = if (isFirstSession) "Welcome, $name" else "Welcome back, $name"
        val greetingIllustration = when (currentThemeMode) {
            AtaraxiaThemeMode.LIGHT -> "💮"
            AtaraxiaThemeMode.AURORA -> "✨"
            AtaraxiaThemeMode.COSMOS -> "🌙"
            AtaraxiaThemeMode.FOREST -> "🌿"
            AtaraxiaThemeMode.SAKURA -> "🌸"
            AtaraxiaThemeMode.AQUA -> "🌊"
        }
        GreetingCard(
            greeting = greetingPrefix,
            date = "Take your time. Today is a clean slate.",
            illustration = {
                Text(
                    text = greetingIllustration,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        )

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

        QuoteCard(
            quote = currentQuote,
            onRefresh = {
                val quotes = listOf(
                    "One gentle breath can change everything.",
                    "Slow down. Your next breath is enough.",
                    "Be here. You are exactly where you need to be.",
                    "Quiet your heart, listen to the silent space within.",
                    "Peace is not the absence of trouble, but the presence of calm."
                )
                currentQuote = quotes.filter { it != currentQuote }.random()
            }
        )

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        LunafloraCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SectionHeader(
                    title = "How are you feeling?",
                    subtitle = "Check in with yourself right now."
                )
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
                ) {
                    val moods = listOf(
                        "Peaceful" to "🌸",
                        "Grateful" to "🌱",
                        "Tired" to "🌙",
                        "Anxious" to "☁️",
                        "Energetic" to "🔥",
                        "Reflective" to "🍃",
                        "Comforted" to "🧸",
                        "Calm" to "🌊"
                    )
                    moods.forEach { (label, emoji) ->
                        MoodChip(
                            moodEmoji = emoji,
                            moodLabel = label,
                            isSelected = selectedMood == label,
                            onClick = { selectedMood = if (selectedMood == label) "" else label }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space24))

        LunafloraCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SectionHeader(
                    title = "Quick Sanctuary",
                    subtitle = "Where would you like to rest today?"
                )
                
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                ) {
                    SanctuaryRow(
                        icon = "🌸",
                        title = "Breathe Space",
                        subtitle = "Slow down with guided breathing.",
                        onClick = onQuickBreathe
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                    
                    SanctuaryRow(
                        icon = "📖",
                        title = "Write Diary",
                        subtitle = "Share your quiet thoughts privately.",
                        onClick = onQuickJournal
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                    
                    SanctuaryRow(
                        icon = "⏳",
                        title = "Study Sanctuary",
                        subtitle = "Dedicating uninterrupted quiet time.",
                        onClick = onQuickFocus
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }
}

@Composable
private fun SanctuaryRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = AtaraxiaTheme.spacing.Space12, horizontal = AtaraxiaTheme.spacing.Space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = DesignTokens.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = DesignTokens.TextSecondary
            )
        }
    }
}
