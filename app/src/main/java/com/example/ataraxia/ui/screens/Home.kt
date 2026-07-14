package com.example.ataraxia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ataraxia.ui.components.GreetingCard
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.MoodChip
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.QuoteCard
import com.example.ataraxia.ui.components.SectionHeader
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.AtaraxiaThemeMode
import com.example.ataraxia.ui.theme.DesignTokens

@Composable
fun HomeScreen(
    name: String,
    profileImage: String,
    isFirstLaunch: Boolean,
    currentThemeMode: AtaraxiaThemeMode,
    onFirstLaunchCompleted: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onQuickBreathe: () -> Unit,
    onQuickJournal: () -> Unit,
    onQuickFocus: () -> Unit
) {
    val scrollState = rememberScrollState()
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
        // 1. Logo / Top Bar
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
            ProfileAvatar(
                name = name,
                imageUri = profileImage,
                size = 40.dp,
                onClick = onNavigateToProfile
            )
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // 2. Dynamic Clock-Aware Greeting (Remembered to prevent immediate flickering on first launch completion)
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

        // 3. Calming Daily Affirmations
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

        // 4. Emotional check-in selection
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

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

        // 5. Gentle Actions (Sanctuary entry points)
        SectionHeader(
            title = "Quick Sanctuary",
            subtitle = "Where would you like to rest today?"
        )
        
        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)
        ) {
            LunafloraCard(
                onClick = onQuickBreathe,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌸", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
                    Column {
                        Text(
                            text = "Breathe Space",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = DesignTokens.TextPrimary
                        )
                        Text(
                            text = "Slow down with guided breathing.",
                            style = MaterialTheme.typography.labelLarge,
                            color = DesignTokens.TextSecondary
                        )
                    }
                }
            }
            
            LunafloraCard(
                onClick = onQuickJournal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
                    Column {
                        Text(
                            text = "Write Diary",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = DesignTokens.TextPrimary
                        )
                        Text(
                            text = "Share your quiet thoughts privately.",
                            style = MaterialTheme.typography.labelLarge,
                            color = DesignTokens.TextSecondary
                        )
                    }
                }
            }
            
            LunafloraCard(
                onClick = onQuickFocus,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space16))
                    Column {
                        Text(
                            text = "Study Sanctuary",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = DesignTokens.TextPrimary
                        )
                        Text(
                            text = "Dedicating uninterrupted quiet time.",
                            style = MaterialTheme.typography.labelLarge,
                            color = DesignTokens.TextSecondary
                        )
                    }
                }
            }
        }

        // Floating nav bar layout spacer
        Spacer(modifier = Modifier.height(130.dp))
    }
}
