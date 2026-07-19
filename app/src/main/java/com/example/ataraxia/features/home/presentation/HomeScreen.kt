package com.example.ataraxia.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import java.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.MoodChip
import com.example.ataraxia.ui.components.ScreenEnclosure
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.viewmodel.MainViewModel
import com.example.ataraxia.viewmodel.RecentActivityItem
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    onQuickBreathe: () -> Unit,
    onQuickJournal: (String?) -> Unit,
    onQuickFocus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    scrollToTopKey: Int = 0
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    // Collect viewmodel flows
    val userName by mainViewModel.username.collectAsState()
    val isFirstLaunch by mainViewModel.isFirstLaunch.collectAsState()
    val greetingInfo by mainViewModel.greeting.collectAsState()
    val todayMood by mainViewModel.todayMood.collectAsState()

    val initialFirstLaunch = remember { isFirstLaunch }

    // Aggregations
    val todayJournalCount by mainViewModel.todayJournalCount.collectAsState()
    val todayFocusMinutes by mainViewModel.todayFocusMinutes.collectAsState()
    val todayBreatheMinutes by mainViewModel.todayBreatheMinutes.collectAsState()
    val daysActive by mainViewModel.daysActive.collectAsState()
    val thisWeeksReflectionsCount by mainViewModel.thisWeeksReflectionsCount.collectAsState()
    val totalBreatheMinutes by mainViewModel.totalBreatheMinutes.collectAsState()
    val recentActivity by mainViewModel.recentActivity.collectAsState()

    // Refresh greeting and onboarding checks on view load
    LaunchedEffect(Unit) {
        mainViewModel.refreshGreeting()
        if (isFirstLaunch) {
            mainViewModel.completeFirstLaunch()
        }
    }

    // Scroll to top
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0) scrollState.animateScrollTo(0)
    }

    // Animated visibility trigger for greeting and phases
    var isGreetingVisible by remember { mutableStateOf(false) }
    var greetingPhase by remember { mutableStateOf(1) }

    LaunchedEffect(greetingInfo) {
        isGreetingVisible = false
        greetingPhase = 1
        delay(50.milliseconds)
        isGreetingVisible = true
        delay(3000.milliseconds)
        greetingPhase = 2
        delay(2000.milliseconds)
        greetingPhase = 3
    }

    var isEditingMood by remember { mutableStateOf(false) }

    // Calm Tips List Rotator
    val calmTips = remember {
        listOf(
            "Small steps are still progress.",
            "Your thoughts deserve kindness.",
            "Rest is productive too.",
            "Take a single deep breath right now.",
            "Quiet your heart and listen to the space within.",
            "Be here. You are exactly where you need to be.",
            "Slow down. Your next breath is enough."
        )
    }
    val todayTip = remember { calmTips.random() }

    ScreenEnclosure {
        // App bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = "Ataraxia Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = "Ataraxia",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
        ) {


        // Section 1: Dynamic Greeting System
        AnimatedVisibility(
            visible = isGreetingVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
                    )
        ) {
            AnimatedContent(
                targetState = greetingPhase,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500)) { it / 3 }) togetherWith
                    (fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300)) { -it / 3 })
                },
                label = "GreetingTransition"
            ) { phase ->
                val titleText: String
                val subtitleText: String

                when (phase) {
                    1 -> {
                        titleText = if (initialFirstLaunch) "Welcome," else "Welcome back,"
                        subtitleText = userName.ifEmpty { "friend" }
                    }
                    2 -> {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        titleText = when (hour) {
                            in 5..11 -> "Today is a clean slate"
                            in 12..16 -> "Pause, breathe, and reset"
                            in 17..20 -> "Time to gently unwind"
                            else -> "Rest, heal, and restore"
                        }
                        subtitleText = when (hour) {
                            in 5..11 -> "Embrace the fresh energy."
                            in 12..16 -> "Take a moment to center yourself."
                            in 17..20 -> "Let go of the day's weight."
                            else -> "You've done enough today."
                        }
                    }
                    else -> {
                        titleText = greetingInfo.title
                        subtitleText = greetingInfo.subtitle
                    }
                }

                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DesignTokens.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 2: Daily Reflection Card (Second largest)
        LunafloraCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onQuickJournal(mainViewModel.dailyPrompt)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AtaraxiaTheme.spacing.Space16)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Reflection",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "✍️", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = mainViewModel.dailyPrompt,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                Text(
                    text = "Tap to begin writing in your diary",
                    style = MaterialTheme.typography.labelSmall,
                    color = DesignTokens.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 3: Mood Check-in
        if (todayMood.isNotEmpty() && !isEditingMood) {
            LunafloraCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AtaraxiaTheme.spacing.Space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your Mood Today",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DesignTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🌿 $todayMood",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isEditingMood = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            LunafloraCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "How are you feeling?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Text(
                        text = "Check in with yourself right now.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                    
                    // Expanded mood selections: 16 items containing mix of positive, neutral, negative/heavy states
                    val moods = listOf(
                        "Peaceful" to "🌸",
                        "Grateful" to "🌱",
                        "Calm" to "🌊",
                        "Hopeful" to "✨",
                        "Joyful" to "☀️",
                        "Comforted" to "🧸",
                        "Reflective" to "🍃",
                        "Energetic" to "🔥",
                        "Tired" to "🌙",
                        "Anxious" to "☁️",
                        "Stressed" to "🤯",
                        "Sad" to "😢",
                        "Angry" to "💢",
                        "Overwhelmed" to "🌀",
                        "Lonely" to "👤",
                        "Sleepy" to "💤"
                    )

                    // Infinite horizontal scroll displaying exactly 2.5 mood items in each of the 2 rows
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val cardWidth = maxWidth
                        val spacing = 8.dp
                        // Exactly 2.5 columns visible -> cardWidth = 2.5 * chipWidth + 2 * spacing
                        val chipWidth = (cardWidth - (spacing * 2)) / 2.5f
                        val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = 10000 - (10000 % moods.size))

                        LazyHorizontalGrid(
                            rows = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(108.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                            verticalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            items(20000) { globalIndex ->
                                val index = globalIndex % moods.size
                                val (label, emoji) = moods[index]
                                val isSelected = todayMood == label
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.05f else 1.0f,
                                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                    label = "MoodChipScale"
                                )
                                MoodChip(
                                    moodEmoji = emoji,
                                    moodLabel = label,
                                    isSelected = isSelected,
                                    modifier = Modifier
                                        .width(chipWidth)
                                        .scale(scale),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        mainViewModel.saveTodayMood(label)
                                        isEditingMood = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 4: Quick Sanctuary
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
            ) {
                SanctuaryTile(
                    icon = "📖",
                    title = "Diary",
                    subtitle = "Write thoughts",
                    onClick = { onQuickJournal(null) },
                    modifier = Modifier.weight(1f)
                )
                SanctuaryTile(
                    icon = "🌸",
                    title = "Breathe",
                    subtitle = "Mindful pause",
                    onClick = onQuickBreathe,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
            ) {
                SanctuaryTile(
                    icon = "🎯",
                    title = "Focus",
                    subtitle = "Deep work",
                    onClick = onQuickFocus,
                    modifier = Modifier.weight(1f)
                )
                SanctuaryTile(
                    icon = "⚙️",
                    title = "Settings",
                    subtitle = "Personalize",
                    onClick = onNavigateToProfile,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 5: Today's Overview
        LunafloraCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AtaraxiaTheme.spacing.Space16)
            ) {
                Text(
                    text = "Today's Sanctuary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DesignTokens.TextPrimary
                )
                Text(
                    text = "A live view of your progress today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignTokens.TextSecondary
                )
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OverviewStat(label = "Reflections", value = "$todayJournalCount", icon = "📖", modifier = Modifier.weight(1f))
                    OverviewStat(label = "Focus", value = "${todayFocusMinutes}m", icon = "⏳", modifier = Modifier.weight(1f))
                    OverviewStat(label = "Breathe", value = "${todayBreatheMinutes}m", icon = "🌸", modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 8 & 9: Welcoming Onboarding / Stats Preview
        if (daysActive == 0) {
            LunafloraCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "Welcome to Ataraxia",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                    Text(
                        text = "Ataraxia is your local, offline sanctuary designed to bring calm and mindfulness to your daily routine. Take your first step:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DesignTokens.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                    OnboardingStep(icon = "📖", title = "Reflections Diary", desc = "Write down your private thoughts, attach voice notes or photos.")
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                    OnboardingStep(icon = "🌸", title = "Guided Breathing", desc = "Calm your mind using lotus breathing animations.")
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                    OnboardingStep(icon = "🎯", title = "Focus Sanctuary", desc = "Set countdown timers or use Flow Mode for study sessions.")
                }
            }
        } else {
            LunafloraCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "Sanctuary Stats",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Days Active", value = "$daysActive", modifier = Modifier.weight(1f))
                        StatItem(label = "Today's Focus", value = "${todayFocusMinutes}m", modifier = Modifier.weight(1f))
                        StatItem(label = "This Week", value = "$thisWeeksReflectionsCount", modifier = Modifier.weight(1f))
                        StatItem(label = "Breathing", value = "${totalBreatheMinutes}m", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

        // Section 6: Recent Activity
        if (recentActivity.isNotEmpty()) {
            LunafloraCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AtaraxiaTheme.spacing.Space16)
                ) {
                    Text(
                        text = "Recently",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DesignTokens.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                        recentActivity.take(3).forEachIndexed { index, item ->
                            RecentActivityRow(
                                item = item,
                                onClick = {
                                    when (item.type) {
                                        "Journal" -> onQuickJournal(null)
                                        "Breathe" -> onQuickBreathe()
                                        "Focus" -> onQuickFocus()
                                    }
                                }
                            )
                            if (index < recentActivity.take(3).size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))
        }

        // Section 7: Calm Tip
        LunafloraCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AtaraxiaTheme.spacing.Space16),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌿", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                Text(
                    text = todayTip,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = DesignTokens.TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SanctuaryTile(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "TileScale"
    )

    LunafloraCard(
        modifier = modifier
            .scale(scale)
            .height(110.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = DesignTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = DesignTokens.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OverviewStat(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary
        )
    }
}

@Composable
private fun OnboardingStep(icon: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextPrimary)
            Text(desc, style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentActivityRow(
    item: RecentActivityItem,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (item.type) {
                    "Journal" -> "📖"
                    "Breathe" -> "🌸"
                    else -> "🎯"
                },
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = DesignTokens.TextPrimary
                )
                Text(
                    text = formatRelativeTime(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = DesignTokens.TextSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = DesignTokens.TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff < 60 * 1000) return "Just now"
    if (diff < 60 * 60 * 1000) return "${diff / (60 * 1000)}m ago"
    if (diff < 24 * 60 * 60 * 1000) return "${diff / (60 * 60 * 1000)}h ago"
    val days = diff / (24 * 60 * 60 * 1000)
    if (days == 1L) return "Yesterday"
    return "${days}d ago"
}
