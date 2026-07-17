package com.example.ataraxia.features.breathe.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.ui.components.EmptyState
import com.example.ataraxia.ui.components.AtaraxiaPrimaryButton
import com.example.ataraxia.ui.components.AtaraxiaSecondaryButton
import com.example.ataraxia.ui.components.LunafloraCard
import com.example.ataraxia.ui.theme.AtaraxiaTheme
import com.example.ataraxia.ui.theme.DesignTokens
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BreatheScreen(
    viewModel: BreatheViewModel,
    onSessionActiveChanged: (Boolean) -> Unit,
    scrollToTopKey: Int = 0
) {
    val sessions by viewModel.allSessions.collectAsState()

    var isSessionActive by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }

    var showCalendarDialog by remember { mutableStateOf(false) }
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDayNum by remember { mutableStateOf<Int?>(null) }

    val displayedSessions = remember(sessions, selectedDayNum, calendarMonth, calendarYear) {
        if (selectedDayNum == null) {
            sessions
        } else {
            sessions.filter { session ->
                val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
                sessionCal.get(Calendar.YEAR) == calendarYear &&
                sessionCal.get(Calendar.MONTH) == calendarMonth &&
                sessionCal.get(Calendar.DAY_OF_MONTH) == selectedDayNum
            }
        }
    }

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
    var selectedDurationMinutes by remember { mutableStateOf(5) }

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

    fun logSessionAndReset(duration: Int, method: String, mood: String) {
        viewModel.logSession(duration, method, mood)
        selectedMood = ""
        showMoodPicker = false
    }

    val idleScrollState = rememberScrollState()
    LaunchedEffect(scrollToTopKey) {
        if (scrollToTopKey > 0 && !isSessionActive) idleScrollState.animateScrollTo(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTokens.AppBackground)
    ) {
        if (!isSessionActive && !showMoodPicker) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = AtaraxiaTheme.spacing.Space24)
                    .verticalScroll(idleScrollState)
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
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space12))

                // Stats Banner
                val todayMins = remember(sessions) { calculateTodayBreatheMinutes(sessions) }
                val weekMins  = remember(sessions) { calculateWeekBreatheMinutes(sessions) }
                val monthMins = remember(sessions) { calculateMonthBreatheMinutes(sessions) }
                val currentStreak = remember(sessions) { calculateBreatheStreak(sessions) }

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                val currentMethod = remember(selectedMethod) {
                    methods.firstOrNull { it.name == selectedMethod } ?: methods.first()
                }

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space20)) {
                        Column {
                            Text(
                                text = "Breathing Method",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = DesignTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f))
                                    .clickable { showMethodPopup = true }
                                    .padding(AtaraxiaTheme.spacing.Space12)
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
                                        Text(
                                            text = currentMethod.desc,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DesignTokens.TextSecondary
                                        )
                                    }
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
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target Duration",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = DesignTokens.TextSecondary
                                )
                                Text(
                                    text = "$selectedDurationMinutes min",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space8))
                            Slider(
                                value = selectedDurationMinutes.toFloat(),
                                onValueChange = { selectedDurationMinutes = it.toInt().coerceIn(1, 30) },
                                valueRange = 1f..30f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = DesignTokens.TextSecondary.copy(alpha = 0.2f)
                                )
                            )
                        }

                        AtaraxiaPrimaryButton(
                            text = "Begin Session",
                            onClick = {
                                pendingMethod = selectedMethod
                                isSessionActive = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AtaraxiaTheme.spacing.Space32))

                LunafloraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(AtaraxiaTheme.spacing.Space16)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val monthName = DateFormatSymbols().months[calendarMonth]
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedDayNum != null) "Breathe Logs: $monthName $selectedDayNum" else "Recent Sessions",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DesignTokens.TextPrimary
                                )
                                Text(
                                    text = if (selectedDayNum != null) "Breathing logs on selected calendar day." else "Your breathing history.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DesignTokens.TextSecondary
                                )
                            }
                            if (selectedDayNum != null) {
                                Text(
                                    text = "Show All",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { selectedDayNum = null }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            } else {
                                IconButton(onClick = { showCalendarDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarMonth,
                                        contentDescription = "View Calendar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        if (displayedSessions.isEmpty()) {
                            EmptyState(
                                illustration = {
                                    Icon(
                                        imageVector = Icons.Outlined.Spa,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                },
                                title = if (selectedDayNum != null) "No sessions today" else "No breathe sessions logged yet",
                                subtitle = if (selectedDayNum != null) "You didn't log any breathe sessions on this day." else "Begin a breathing session to start anchoring yourself in the present."
                            )
                        } else {
                            BreatheHistoryTimeline(sessions = displayedSessions, onDelete = { id -> viewModel.deleteSession(id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(130.dp))
            }
        } else if (isSessionActive) {
            BreatheActiveSession(
                pendingMethod = pendingMethod,
                selectedMethod = selectedMethod,
                onEndSession = { elapsed, method ->
                    if (elapsed >= 10) {
                        pendingDurationSeconds = elapsed
                        pendingMethod = method
                        isSessionActive = false
                        showMoodPicker = true
                    } else {
                        isSessionActive = false
                    }
                }
            )
        }

        // Mood Picker
        AnimatedVisibility(
            visible = showMoodPicker,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
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
                                logSessionAndReset(pendingDurationSeconds, pendingMethod, "")
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AtaraxiaPrimaryButton(text = "Save", onClick = {
                                logSessionAndReset(pendingDurationSeconds, pendingMethod, selectedMood)
                            })
                        }
                    }
                }
            }
        }

        // Method Selector Dialog
        BreatheMethodSelector(
            showMethodPopup = showMethodPopup,
            selectedMethod = selectedMethod,
            methods = methods,
            onDismiss = { showMethodPopup = false },
            onMethodSelected = { selectedMethod = it }
        )

        // Calendar Dialog
        BreatheCalendarDialog(
            showCalendarDialog = showCalendarDialog,
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            selectedDayNum = selectedDayNum,
            sessions = sessions,
            onDismiss = { showCalendarDialog = false },
            onMonthChanged = { month, year ->
                calendarMonth = month
                calendarYear = year
                selectedDayNum = null
            },
            onDaySelected = { day ->
                selectedDayNum = day
                showCalendarDialog = false
            }
        )
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

// ── Stats Helpers ──
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
    } catch (_: Exception) {
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
