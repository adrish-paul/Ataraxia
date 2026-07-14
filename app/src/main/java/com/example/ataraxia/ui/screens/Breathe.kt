package com.example.ataraxia.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.ui.components.AnimatedLotus
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.BreatheState
import com.example.ataraxia.ui.components.DurationChip
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.components.ProfileAvatar
import com.example.ataraxia.ui.components.SectionHeader
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import com.example.ataraxia.viewmodel.BreatheViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BreatheScreen(
    name: String,
    profileImage: String,
    viewModel: BreatheViewModel,
    onSessionActiveChanged: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val sessions by viewModel.allSessions.collectAsState()

    var isSessionActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    var showMoodPicker by remember { mutableStateOf(false) }
    var pendingDurationSeconds by remember { mutableStateOf(0) }
    var pendingMethod by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("") }

    LaunchedEffect(isSessionActive) {
        if (isSessionActive) elapsedSeconds = 0
        onSessionActiveChanged(isSessionActive)
    }

    val view = LocalView.current
    DisposableEffect(isSessionActive) {
        if (isSessionActive) view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    var selectedMethod by remember { mutableStateOf("Box Breathing") }
    var selectedDuration by remember { mutableStateOf("5 Min") }
    var breathInhaleSeconds by remember { mutableStateOf(4f) }

    val methods = listOf(
        MethodItem("Box Breathing",       "Equal ratios of inhale, hold, exhale, hold.",                 "4 - 4 - 4 - 4"),
        MethodItem("Deep Calm",            "Extended exhalations for rapid nerve grounding.",             "4 - 7 - 8"),
        MethodItem("Triangle Breathing",   "Inhale, hold, exhale. Military-proven clarity.",              "4 - 4 - 4"),
        MethodItem("Resonance Breathing",  "5.5 s cycles for heart-rate coherence.",                      "5.5 - 5.5"),
        MethodItem("Sleep Breathing",      "Dr. Weil's sleep induction — long hold, slow release.",       "4 - 7 - 8"),
        MethodItem("Cleansing Breath",     "Short centering pattern focusing on gentle release.",         "4 - 2 - 4"),
        MethodItem("Calm Breathing",       "Balanced inhale and exhale for everyday calm.",               "5 - 5")
    )

    var showMethodPopup by remember { mutableStateOf(false) }

    fun endSessionWithMood() {
        if (elapsedSeconds >= 10) {
            pendingDurationSeconds = elapsedSeconds
            isSessionActive = false
            showMoodPicker = true
        } else {
            isSessionActive = false
        }
    }

    BackHandler(enabled = isSessionActive) { endSessionWithMood() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
    ) {
        // -- Idle / Selection screen -----------------------------------------
        if (!isSessionActive && !showMoodPicker) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AtaraxiaTheme.spacing.Space16),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFlorist,
                            contentDescription = "Breathe",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                        Text(
                            text = "Breathe",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                            color = DesignTokens.TextPrimary
                        )
                    }
                    ProfileAvatar(name = name, imageUri = profileImage, size = 40.dp, onClick = onNavigateToProfile)
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                // Stats Banner (Meditation stats)
                val todayMins = remember(sessions) { calculateTodayBreatheMinutes(sessions) }
                val weekMins  = remember(sessions) { calculateWeekBreatheMinutes(sessions) }
                val monthMins = remember(sessions) { calculateMonthBreatheMinutes(sessions) }
                val currentStreak = remember(sessions) { calculateBreatheStreak(sessions) }

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Streak row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (currentStreak > 0) "🔥" else "✨",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (currentStreak > 0) "$currentStreak Day Streak" else "Start your first session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BreatheStatCell(label = "Today", value = formatBreatheTime(todayMins))
                            BreatheStatDivider()
                            BreatheStatCell(label = "This Week", value = formatBreatheTime(weekMins))
                            BreatheStatDivider()
                            BreatheStatCell(label = "This Month", value = formatBreatheTime(monthMins))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                SectionHeader(title = "Choose Breathing Method", subtitle = "Pick a rhythm that matches your state.")
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))

                val currentMethod = remember(selectedMethod) {
                    methods.firstOrNull { it.name == selectedMethod } ?: methods.first()
                }

                LunafloraCard(
                    onClick = { showMethodPopup = true },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = DesignTokens.CardBackground,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentMethod.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentMethod.desc,
                                style = MaterialTheme.typography.labelMedium,
                                color = DesignTokens.TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentMethod.pattern,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space8))
                            Icon(
                                imageVector = Icons.Outlined.UnfoldMore,
                                contentDescription = "Select Method",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                              )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                SectionHeader(title = "Target Duration", subtitle = "Decide on a duration for your session.")
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                    listOf("2 Min", "5 Min", "10 Min").forEach { duration ->
                        Box(modifier = Modifier.weight(1f)) {
                            DurationChip(durationText = duration, isSelected = selectedDuration == duration, onClick = { selectedDuration = duration })
                        }
                    }
                }

                if (selectedMethod != "Resonance Breathing") {
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                    SectionHeader(title = "Breathe Speed", subtitle = "Adjust your target inhale duration.")
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                    Text(text = "Inhale Cycle: ${breathInhaleSeconds.toInt()} Seconds", style = MaterialTheme.typography.labelLarge, color = DesignTokens.TextSecondary)
                    Slider(
                        value = breathInhaleSeconds, onValueChange = { breathInhaleSeconds = it },
                        valueRange = 3f..8f, steps = 4,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                AtaraxiaPrimaryButton(text = "Begin Session", onClick = {
                    pendingMethod = selectedMethod
                    isSessionActive = true
                })

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                SectionHeader(title = "Recent Sessions", subtitle = "Your breathing history.")
                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                if (sessions.isEmpty()) {
                    EmptyState(
                        illustration = {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        },
                        title = "No breathe sessions logged yet",
                        subtitle = "Begin a breathing session to start anchoring yourself in the present."
                    )
                } else {
                    BreatheHistoryTimeline(sessions = sessions, onDelete = { id -> viewModel.deleteSession(id) })
                }

                Spacer(modifier = Modifier.height(130.dp))
            }

        } else if (isSessionActive) {
            // -- Active session screen -------------------------------------
            var currentState by remember { mutableStateOf(BreatheState.REST) }
            var currentInstruction by remember { mutableStateOf("Prepare...") }
            var currentCounter by remember { mutableStateOf(2) }
            var completedCycles by remember { mutableStateOf(0) }

            LaunchedEffect(key1 = true) {
                val method = pendingMethod.ifEmpty { selectedMethod }
                val isResonance = method == "Resonance Breathing"

                // Timing in ms
                val inhaleMs: Long = if (isResonance) 5500L else breathInhaleSeconds.toLong() * 1000L
                val holdMs: Long = when (method) {
                    "Box Breathing"      -> breathInhaleSeconds.toLong() * 1000L
                    "Triangle Breathing" -> breathInhaleSeconds.toLong() * 1000L
                    "Deep Calm"          -> (breathInhaleSeconds * 1750).toLong()
                    "Sleep Breathing"    -> 7000L
                    "Cleansing Breath"   -> (breathInhaleSeconds * 500).toLong()
                    else                 -> 0L
                }
                val exhaleMs: Long = when (method) {
                    "Box Breathing"       -> breathInhaleSeconds.toLong() * 1000L
                    "Triangle Breathing"  -> breathInhaleSeconds.toLong() * 1000L
                    "Deep Calm"           -> (breathInhaleSeconds * 2000).toLong()
                    "Sleep Breathing"     -> 8000L
                    "Resonance Breathing" -> 5500L
                    else                  -> breathInhaleSeconds.toLong() * 1000L
                }
                val pauseMs: Long = if (method == "Box Breathing") breathInhaleSeconds.toLong() * 1000L else 0L

                currentInstruction = "Prepare..."
                currentCounter = 2
                delay(2000)

                while (isActive) {
                    // Inhale
                    currentState = BreatheState.INHALE
                    currentInstruction = "Breathe In..."
                    val inhaleSteps = if (isResonance) 11 else breathInhaleSeconds.toInt()
                    val inhaleTickMs = inhaleMs / inhaleSteps.coerceAtLeast(1)
                    for (i in inhaleSteps downTo 1) { currentCounter = i; delay(inhaleTickMs); elapsedSeconds++ }

                    // Hold
                    if (holdMs > 0L) {
                        currentState = BreatheState.HOLD
                        currentInstruction = "Hold..."
                        val holdSecs = (holdMs / 1000).toInt().coerceAtLeast(1)
                        for (i in holdSecs downTo 1) { currentCounter = i; delay(1000); elapsedSeconds++ }
                    }

                    // Exhale
                    currentState = BreatheState.EXHALE
                    currentInstruction = "Breathe Out..."
                    val exhaleSecs = (exhaleMs / 1000).toInt().coerceAtLeast(1)
                    val exhaleTickMs = exhaleMs / exhaleSecs.coerceAtLeast(1)
                    for (i in exhaleSecs downTo 1) { currentCounter = i; delay(exhaleTickMs); elapsedSeconds++ }

                    // Pause
                    if (pauseMs > 0L) {
                        currentState = BreatheState.REST
                        currentInstruction = "Rest..."
                        val pauseSecs = (pauseMs / 1000).toInt().coerceAtLeast(1)
                        for (i in pauseSecs downTo 1) { currentCounter = i; delay(1000); elapsedSeconds++ }
                    }

                    completedCycles++
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .padding(vertical = AtaraxiaTheme.spacing.Space16),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { endSessionWithMood() }) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "End Session", tint = DesignTokens.TextPrimary)
                    }
                    Text(text = pendingMethod.ifEmpty { selectedMethod }, style = MaterialTheme.typography.labelLarge, color = DesignTokens.TextSecondary)
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                    AnimatedLotus(breatheState = currentState, cycleDurationMs = (breathInhaleSeconds * 300).toInt(), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))
                    Text(text = currentInstruction, style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                    Text(text = "$currentCounter", style = MaterialTheme.typography.headlineLarge, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space16))
                    Text(text = "Cycles: $completedCycles", style = MaterialTheme.typography.labelLarge, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)
                }

                AtaraxiaSecondaryButton(text = "End Session", onClick = { endSessionWithMood() }, modifier = Modifier.padding(bottom = AtaraxiaTheme.spacing.Space16))
            }
        }

        // -- Mood Picker -----------------------------------------------------
        AnimatedVisibility(
            visible = showMoodPicker,
            enter = slideInVertically(initialOffsetY = { it }),
            exit  = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(modifier = Modifier.fillMaxSize().background(DesignTokens.AppBackground), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AtaraxiaTheme.spacing.Space24),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space24)
                ) {
                    Text(text = "How do you feel?", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary, textAlign = TextAlign.Center)
                    Text(text = "Take a moment to note your state.", style = MaterialTheme.typography.bodyLarge, color = DesignTokens.TextSecondary, textAlign = TextAlign.Center)

                    val moods = listOf("😌 Calm", "😴 Sleepy", "😊 Happy", "😤 Relieved", "🤔 Neutral")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        moods.take(3).forEach { mood ->
                            MoodChip(mood = mood, isSelected = selectedMood == mood, onClick = { selectedMood = if (selectedMood == mood) "" else mood }, modifier = Modifier.weight(1f))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        moods.drop(3).forEach { mood ->
                            MoodChip(mood = mood, isSelected = selectedMood == mood, onClick = { selectedMood = if (selectedMood == mood) "" else mood }, modifier = Modifier.weight(1f))
                        }
                        Box(modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space12)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaSecondaryButton(text = "Skip", onClick = {
                                viewModel.logSession(pendingDurationSeconds, pendingMethod.ifEmpty { selectedMethod }, "")
                                selectedMood = ""; showMoodPicker = false
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaPrimaryButton(text = "Save", onClick = {
                                viewModel.logSession(pendingDurationSeconds, pendingMethod.ifEmpty { selectedMethod }, selectedMood)
                                selectedMood = ""; showMoodPicker = false
                            })
                        }
                    }
                }
            }
        }

        // ── Breathing Method Selection Popup ────────────────────────────────
        if (showMethodPopup) {
            Dialog(onDismissRequest = { showMethodPopup = false }) {
                LunafloraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AtaraxiaTheme.spacing.Space8)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Breathing Method",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DesignTokens.TextPrimary
                            )
                            IconButton(
                                onClick = { showMethodPopup = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = DesignTokens.TextSecondary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)
                        ) {
                            methods.forEach { method ->
                                val isSelected = selectedMethod == method.name
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            selectedMethod = method.name
                                            showMethodPopup = false
                                        }
                                        .padding(AtaraxiaTheme.spacing.Space12)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Outlined.LocalFlorist else Icons.Outlined.Spa,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                            Column {
                                                Text(
                                                    text = method.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextPrimary
                                                )
                                                Text(
                                                    text = method.desc,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = DesignTokens.TextSecondary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = method.pattern,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodChip(mood: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else DesignTokens.CardBackground)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = mood.take(2), fontSize = 22.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mood.drop(2).trim(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else DesignTokens.TextSecondary
            )
        }
    }
}

@Composable
private fun BreatheHistoryTimeline(sessions: List<BreatheSessionEntity>, onDelete: (Long) -> Unit) {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val displaySdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val grouped = sessions.groupBy { sdf.format(Date(it.timestamp)) }.toSortedMap(reverseOrder())

    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
        grouped.forEach { (dateKey, daySessions) ->
            val displayDate = try {
                val y = dateKey.substring(0, 4).toInt()
                val m = dateKey.substring(4, 6).toInt() - 1
                val d = dateKey.substring(6, 8).toInt()
                displaySdf.format(Calendar.getInstance().apply { set(y, m, d) }.time)
            } catch (e: Exception) { dateKey }

            Text(text = displayDate, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DesignTokens.TextSecondary, modifier = Modifier.padding(vertical = 2.dp))

            Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space8)) {
                daySessions.forEach { session ->
                    LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(AtaraxiaTheme.spacing.Space12))
                                Column {
                                    Text(text = session.method, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = DesignTokens.TextPrimary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = formatBreatheDuration(session.durationSeconds), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                        if (session.mood.isNotEmpty()) {
                                            Text(text = "·", color = DesignTokens.TextSecondary, style = MaterialTheme.typography.labelMedium)
                                            Text(text = session.mood, style = MaterialTheme.typography.labelMedium, color = DesignTokens.TextSecondary)
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { onDelete(session.id) }) {
                                Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = DesignTokens.TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBreatheDuration(totalSeconds: Int): String = when {
    totalSeconds < 60 -> "< 1 Min"
    else -> { val m = totalSeconds / 60; val s = totalSeconds % 60; if (s == 0) "$m Min" else "$m Min $s Sec" }
}

private data class MethodItem(val name: String, val desc: String, val pattern: String)

// ── Stats Helpers ────────────────────────────────────────────────────────────

private fun calculateBreatheStreak(sessions: List<BreatheSessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val uniqueDays = sessions.map { sdf.format(Date(it.timestamp)).toInt() }
        .distinct()
        .sortedDescending()

    val todayStr = sdf.format(Date()).toInt()
    val cal = Calendar.getInstance()
    cal.add(Calendar.DATE, -1)
    val yesterdayStr = sdf.format(cal.time).toInt()

    if (uniqueDays.first() != todayStr && uniqueDays.first() != yesterdayStr) {
        return 0
    }

    var streak = 1
    val currentDayCal = Calendar.getInstance()
    val newestStr = uniqueDays.first().toString()
    try {
        currentDayCal.set(Calendar.YEAR, newestStr.substring(0, 4).toInt())
        currentDayCal.set(Calendar.MONTH, newestStr.substring(4, 6).toInt() - 1)
        currentDayCal.set(Calendar.DAY_OF_MONTH, newestStr.substring(6, 8).toInt())
    } catch (e: Exception) {
        return 1
    }

    for (i in 1 until uniqueDays.size) {
        currentDayCal.add(Calendar.DATE, -1)
        val expectedDay = sdf.format(currentDayCal.time).toInt()
        if (uniqueDays[i] == expectedDay) {
            streak++
        } else {
            break
        }
    }
    return streak
}

private fun calculateTodayBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val today = sdf.format(Date())
    val totalSeconds = sessions.filter { sdf.format(Date(it.timestamp)) == today }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun calculateWeekBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.timeInMillis
    val totalSeconds = sessions.filter { it.timestamp >= weekStart }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun calculateMonthBreatheMinutes(sessions: List<BreatheSessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val monthStart = cal.timeInMillis
    val totalSeconds = sessions.filter { it.timestamp >= monthStart }
        .sumOf { it.durationSeconds }
    return totalSeconds / 60
}

private fun formatBreatheTime(totalMinutes: Int): String {
    return when {
        totalMinutes == 0 -> "0m"
        totalMinutes < 60 -> "${totalMinutes}m"
        else -> "${totalMinutes / 60}h ${totalMinutes % 60}m".trimEnd().replace("  ", " ")
    }
}

// ── Stat UI Components ────────────────────────────────────────────────────────

@Composable
private fun BreatheStatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
private fun BreatheStatDivider() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(DesignTokens.TextSecondary.copy(alpha = 0.2f))
    )
}
